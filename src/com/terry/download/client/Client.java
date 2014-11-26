package com.terry.download.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.alibaba.fastjson.JSON;
import com.terry.download.common.Commands;
import com.terry.download.common.ListFileRet;
import com.terry.download.common.Utils;

public class Client {
	private static String downloadDir = null;
	private static String serverIP = "127.0.0.1";
	private static int serverPort = 12345;
	public static void main(String[] args) {
		if(args.length == 1){
			downloadDir = args[0];
		}if(args.length == 2) {
			downloadDir = args[0];
			serverIP = args[1];
		} else if(args.length == 3) {
			downloadDir = args[0];
			serverIP = args[1];
			serverPort = Integer.valueOf(args[2]);
		} else {
			System.out.println("./client download_directory [server_ip server_port]");
			return ;
		}
		System.out.println("output directory : " + downloadDir + "\n" + 
				"serverIP :　" + serverIP + "\n" + 
				"serverPort : " + serverPort);

		while(true) {
			Scanner scan = new Scanner(System.in);
			String input = scan.nextLine();
			String[] cmds = input.split("\\s+");
			
			String req = cmds[0];
			if(req.equalsIgnoreCase(Commands.LIST_CMD)) {
				if(cmds.length != 2) {
					System.out.println("list file_or_directory_name");
					continue;
				}
				request(Commands.LIST_DIR , cmds[1]);
			} else if(req.equalsIgnoreCase(Commands.GET_CMD)) {
				if(cmds.length != 2) {
					System.out.println("get file_name");
					continue;
				}
				request(Commands.GET , cmds[1]);
			} else if(req.equalsIgnoreCase(Commands.GET_DIR_CMD)) {
				if(cmds.length != 2) {
					System.out.println("getdir directory_name");
					continue;
				}
				request(Commands.GET_DIR , cmds[1]);
			} else if(req.equalsIgnoreCase(Commands.QUIT)) {
				break;
			} else {
				System.out.println("error command ...");
			}
		}
	}
	
	private static void request(byte cmd , String param) {

		Socket sock = null;
		try {
			sock = new Socket("127.0.0.1" , 12345);
			switch(cmd) {
				case Commands.LIST_DIR :
					requestList(sock , param);
					break;
				case Commands.GET :
					requestGet(sock , downloadDir, param);
					break;
				case Commands.GET_DIR :
					requestGetDir(sock , downloadDir, param);
					break;
				default :
					System.out.println("undefined command : " + cmd);	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				sock.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private static String readAck(int realCmd , InputStream in) {
		byte[] header = new byte[Commands.HEADER_LENGTH];
		if(Utils.readToLength(in, header) != header.length) {
			System.out.println("can not read header !");
			return null;
		}
		
		byte cmd = header[0];
		byte[] lengthBuf = Arrays.copyOfRange(header, 1, header.length - 1);
		int length = Utils.BytesToInt(lengthBuf);
		byte[] buffer = new byte[length];
		if(length <= 0 || Utils.readToLength(in, buffer) != length) {
			System.out.println("can not read data !");
			return null;
		}
		
		if(cmd != realCmd + 1) {
			System.out.println("Error ack command : " + 
					cmd + " original cmd : " + realCmd);			
			return null;
		}
		if(header[header.length - 1] != Commands.SUCEESS) {
			if(buffer.length <= 0) {
				System.out.println("command failed , no reason !");
				return null;
			} else { 
				String reason = new String(buffer , Charset.forName("UTF-8"));
				System.out.println("command failed , reason : " + reason);
				return null;
			}
		}
		
		return new String(buffer , Charset.forName("UTF-8"));
	}
	
	private static String requestAndAck(Socket sock, byte cmd , String content) 
			throws Exception {
			String json = JSON.toJSONString(content);
			byte[] data = Commands.dataPackage(cmd, (byte)0, json);
			OutputStream out;
			out = sock.getOutputStream();
			InputStream in = sock.getInputStream();
			
			out.write(data);
			
			String ack = readAck(cmd , in);
			if(ack == null)
				throw new Exception("command " + cmd + " failed !");
			
			return ack;
	}
	
	private static void requestList(Socket sock , String param) 
			throws Exception {
		String ack = requestAndAck(sock , Commands.LIST_DIR , param);
		
		List<ListFileRet> files = JSON.parseArray(ack, ListFileRet.class);
		if(files != null && !files.isEmpty() && files.get(0).getType() < 0) {
			System.out.println("list " + param + " not exist !");
			return ;
		}
		
		for(ListFileRet file : files) {
			System.out.println(file.toString());
		}
	}
	
	private static void requestGet(Socket sock , String root, String param) 
			throws Exception {
		String ack = requestAndAck(sock , Commands.CHECK_FILE , param);
		
		Long length = JSON.parseObject(ack, Long.class);
		if(length < 0) {
			System.out.println("file " + param + "this is a directory !");
			return ;
		} else {
			System.out.println("get file " + param + " length " + length);
		}
		
		getFile(root, param , length);
	}
	
	private static void requestGetDir(Socket sock , String root, String param) 
			throws Exception {
		String ack = requestAndAck(sock , Commands.LIST_DIR , param);
		List<ListFileRet> files = JSON.parseArray(ack, ListFileRet.class);
		if(files != null && !files.isEmpty() && files.get(0).getType() < 0) {
			System.out.println("list " + param + " not exist !");
			return ;
		}
		
		String[] splits = param.split("\\\\");
		String realName = splits[splits.length - 1];
		String dirName = root + "\\" + realName;
		File dir = new File(dirName);
		if(!dir.exists() && !dir.mkdir()) {
			System.out.println("mkdir " + dirName + " failed !");
			return ;
		}
		for(ListFileRet file : files) {
			if(file.getType() == 0) {
				System.out.println("Starting download file " + file.getName());
				requestGet(sock , dirName,  file.getName());
			} else if(file.getType() == 1) {
				String[] fielSplits = file.getName().split("\\\\");
				String fileName = fielSplits[fielSplits.length - 1];
				System.out.println("file " + file.getName() + " is a directory !");
				requestGetDir(sock , root + "\\" + realName , param + "\\" + fileName);
			}
		}
		System.out.println("download directory " + param + " finished ...");
	}
	
	private static void getFile(String root , String name , long length) 
			throws Exception{
		String[] splits = name.split("\\\\");
		String realName = splits[splits.length - 1];
		int threadNum = 1;
		final long MB = 1024 * 1024;
		if(length <= 50 * MB) {
			threadNum = 1;
		} else if(length <= 500 * MB) {
			threadNum = 2;
		} else if(length <= 2000 * MB) {
			threadNum = 4;
		} else {
			threadNum = 8;
		}
		long perLength = length / threadNum;
		long lastLength = length % threadNum;
		
		String fullPath = root + "\\" + realName;
		List<RequestThread> reqThreads = new LinkedList<RequestThread>();
		for(int i = 0 ; i < threadNum ; ++ i) {
			RequestThread t = new RequestThread(fullPath , name , i * perLength , 
					i == threadNum - 1 ? lastLength + perLength : perLength , serverIP , serverPort);
			t.start();
			reqThreads.add(t);
		}

		while(!waitingForFinish(reqThreads , length)) {
			
		}
		System.out.println("All finish ...");
	}
	
	private static boolean waitingForFinish(List<RequestThread> threads , long length) {
		boolean allFinished = true;
		
		final int gapTime = 1000;
		int perThreadSleep = gapTime / threads.size();
		for(RequestThread t : threads) {
			try {
				t.join(perThreadSleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(t.isAlive()) {
				allFinished = false;
				continue ;
			} else {
				String result = t.getResult();
				if(result != null) {
					System.out.println("thread execute error : " + result);
					stopAll(threads);
					return true;
				} else {
					//防止多次打印进度
					if(!t.checkState()) {
						showState(threads , length);
						t.setState(true);
					}
				}
			}
		}
		showState(threads , length);
		return allFinished;
	}
	
	private static void stopAll(List<RequestThread> threads) {
		for(RequestThread t : threads) {
			if(!t.isAlive()) {
				t.setDone(true);
			}
		}
		
		for(RequestThread t : threads) {
			if(!t.isAlive()) {
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void showState(List<RequestThread> threads , long length) {
		long download = 0;
		for(RequestThread t : threads) {
			download += t.getDownLoadLength();
		}
		
		String format = String.format("download (%.1f %%) : %.2f MB of %.2f MB" , (100 * download) / (double) length , 
				download / (double)(1000 * 1000) , length / (double)(1000 * 1000));
		
		System.out.println(format);
	}
}
