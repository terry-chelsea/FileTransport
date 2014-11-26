package com.terry.download.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSON;
import com.terry.download.common.Commands;
import com.terry.download.common.GetDataParam;
import com.terry.download.common.HeaderAndData;
import com.terry.download.common.Utils;

public class RequestThread extends Thread {
	private String filename = null;
	private String output;
	private long length = 0;
	private long offset = 0;
	private volatile long downloadLen = 0;
	private volatile String result = null;
	private String ip = null;
	private int port = 0;
	private volatile boolean done = false;
	private boolean state = false;
	
	public RequestThread(String output , String name , long offset , long length , String ip , int port) {
		this.filename = name;
		this.length = length;
		this.offset = offset;
		this.ip = ip;
		this.port = port;
		this.output = output;
	}

	public long getDownLoadLength() {
		return downloadLen;
	}
	
	public String getResult() {
		return this.result;
	}
		
	public void setDone(boolean f) {
		this.done = f;
	}
	
	public void setState(boolean st) {
		this.state = st;
	}
	
	public boolean checkState() {
		return this.state;
	}
	
	public void run() {
		Socket sock = null;
		RandomAccessFile fp = null;
		try {
			fp = new RandomAccessFile(output , "rw");
			sock = new Socket(this.ip , this.port);
			InputStream in = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			
			GetDataParam para = new GetDataParam(filename , offset , length);
			String json = JSON.toJSONString(para);
			byte[] data = Commands.dataPackage(Commands.GET_DATA, (byte)0, json);
			
			out.write(data);
			byte[] buffer = this.getData(in);
			while(buffer != null) {
				long bufLen = buffer.length;
//				synchronized(RequestThread.class) {
					fp.seek(offset);
					fp.write(buffer);
					downloadLen += bufLen;
					offset += bufLen;
//				}
				if(done)
					break;
				buffer = this.getData(in);
				if(done)
					break;
			}
			result = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = e.getMessage();
		} finally{
			try {
				fp.close();
				sock.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private byte[] getData(InputStream in) throws Exception{
		HeaderAndData ackData = Commands.readData(in);
		
		if(ackData.getCmd() != Commands.GET_DATA_ACK) {
			System.out.println("get error ack command : " + ackData.getCmd() + 
					" need command : " + Commands.GET_DATA_ACK);
			return null;
		} else if (ackData.getError() != Commands.SUCEESS) {
			if(ackData.getData() == null) {
				System.out.println("get data error : no reason !");
			} else {
				String reason = new String(ackData.getData() , "UTF-8");
				System.out.println("get data error , reason : " + reason);
			}
			return null;
		}
		
		return ackData.getData();
	}
}
