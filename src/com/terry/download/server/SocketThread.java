package com.terry.download.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.terry.download.common.Commands;
import com.terry.download.common.GetDataParam;
import com.terry.download.common.HeaderAndData;
import com.terry.download.common.ListFileRet;
import com.terry.download.common.Utils;

public class SocketThread extends Thread {
	private Socket socket = null;

	public SocketThread(Socket sock) {
		this.socket = sock;
	}
	
	public void run() {
		try {
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			
			while(true) {
				HeaderAndData req = Commands.readData(in);
				if(req == null)
					break;
				dealWithRequest(req , out);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void dealWithRequest(HeaderAndData req , OutputStream out) {
		byte cmd = req.getCmd();
		byte ackCmd = (byte) (cmd + 1);
		byte[] content = req.getData();
		String filePath = null;
		String jsonStr = null;
		File fp = null;
		try {
			switch(cmd) {
				case Commands.LIST_DIR :
					if(content == null) {
						String reason = "input file path is empty !";
						sendErrorMessage(ackCmd , reason , out);
					}
					
					jsonStr = new String(content , "UTF-8");
					filePath = JSON.parseObject(jsonStr, String.class);
					fp = new File(filePath);
					List<ListFileRet> results = new LinkedList<ListFileRet>();
					if(fp.isFile()) {
						ListFileRet ret = new ListFileRet(filePath , 0);
						results.add(ret);
					} else if(fp.isDirectory()) {
						for(String file : fp.list()) {
							String path = filePath + "\\" + file;
							File subFile = new File(path);
							int type = 0;
							if(subFile.isFile()) {
								type = 0;
							} else if(subFile.isDirectory()) {
								type = 1;
							} else 
								type = 2;
							results.add(new ListFileRet(path , type));
						}
					} else {
						ListFileRet ret = new ListFileRet(filePath , -1);
						results.add(ret);
					}
					sendAck(ackCmd, JSON.toJSONString(results) , out);
					break;
				case Commands.CHECK_FILE :
					if(content == null) {
						String reason = "input file path is empty !";
						sendErrorMessage(Commands.LIST_DIR_ACK , reason , out);
					}
					jsonStr = new String(content , "UTF-8");
					filePath = JSON.parseObject(content, String.class);
					fp = new File(filePath);
							
					Long length = 0l;
					if(fp.isFile()) {
						length = fp.length();
					} else if (fp.isDirectory()) {
						length = -1l;
					} else 
						length = -2l;
					sendAck(ackCmd, JSON.toJSONString(length) , out);
					
					break;
				case Commands.GET_DATA :
					if(content == null) {
						String reason = "input file path is empty !";
						sendErrorMessage(Commands.LIST_DIR_ACK , reason , out);
					}
					
					jsonStr = new String(content , "UTF-8");
					GetDataParam para = JSON.parseObject(jsonStr, GetDataParam.class);
					
					sendData(out , para);
					break;
			} 
		} catch (Exception e) {
			e.printStackTrace();
			try {
				sendErrorMessage(ackCmd , e.getMessage() , out);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void sendErrorMessage(byte cmd , String reason , OutputStream out) throws Exception {
		String json = JSON.toJSONString(reason);
		byte[] data = Commands.dataPackage((byte)cmd, Commands.FAILED, json);
		
		out.write(data);
	}
	
	public void sendAck(byte cmd , String json , OutputStream out) throws Exception {
		byte[] data = Commands.dataPackage((byte)cmd, Commands.SUCEESS, json);
		
		out.write(data);
	}
	
	public void sendByteData(OutputStream out , byte[] data , byte cmd) throws Exception{
		if(data == null)
			data = new byte[0];
		byte[] header = Utils.IntegerToBytes(data.length);
		int dataLength = Commands.HEADER_LENGTH + data.length;
		
		byte[] all = new byte[dataLength];
		all[0] = cmd;
		System.arraycopy(header, 0, all, 1, header.length);
		all[Commands.HEADER_LENGTH - 1] = Commands.SUCEESS;
		System.arraycopy(data, 0, all, Commands.HEADER_LENGTH, data.length);
		
		out.write(all);
	}
	
	private void sendData(OutputStream out , GetDataParam para) throws Exception{
		long offset = para.getOffset();
		long length = para.getLength();
		String file = para.getFilepath();
		
		final int lengthPerSend = 1000 * 1000;
		RandomAccessFile fp = new RandomAccessFile(file , "r");
		long lastLength = length;
		byte[] dataBuf = new byte[lengthPerSend];
		while(lastLength > 0) {
			fp.seek(offset);
			int len = fp.read(dataBuf);
			if(len == 0)
				break;
			if(len != dataBuf.length) {
				dataBuf = Arrays.copyOfRange(dataBuf, 0, len);
			}
			
			sendByteData(out , dataBuf , Commands.GET_DATA_ACK);
			offset += len;
			lastLength -= len;
		}
		
		sendByteData(out , null , Commands.GET_DATA_ACK);
		fp.close();
	}
}
