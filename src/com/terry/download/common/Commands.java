package com.terry.download.common;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Commands {
	public static final byte LIST_DIR = 1;
	public static final byte LIST_DIR_ACK = 2;
	public static final byte GET = 5;
	public static final byte GET_ACK = 6;
	public static final byte GET_DIR = 7;
	public static final byte GET_DIR_ACK = 8;
	
	public static final byte CHECK_FILE = 9;;
	public static final byte CHECK_FILE_ACK = 10;

	public static final byte GET_DATA = 3;
	public static final byte GET_DATA_ACK = 4;
	//error code
	public static final byte SUCEESS = 127;
	public static final byte FAILED = 126;

	public static final String LIST_CMD = "list";
	public static final String GET_CMD = "get";
	public static final String GET_DIR_CMD = "getdir";
	public static final String QUIT = "quit";
	
	//头部长度,一个字节的命令id，四个字节的data长度，1个字节的error信息。
	public static final int HEADER_LENGTH = 1 + 4 + 1;
	
	public static byte[] dataPackage(byte command, byte error, String data) {
		byte[] content = new byte[0];
		if(data != null || !data.isEmpty())
			content = data.getBytes(Charset.forName("UTF-8"));
		byte[] header = Utils.IntegerToBytes(content.length);
		int dataLength = 1 + content.length + header.length + 1;
		
		byte[] all = new byte[dataLength];
		all[0] = command;
		System.arraycopy(header, 0, all, 1, header.length);
		all[1 + header.length] = error;
		System.arraycopy(content, 0, all, HEADER_LENGTH, content.length);
		return all;
	}
	
	public static HeaderAndData readData(InputStream in) throws Exception {
		byte[] header = new byte[Commands.HEADER_LENGTH];
		int readLen = Utils.readToLength(in, header);
		if(readLen <= 0) {
			return null;
		} if(readLen != header.length) {
			throw new Exception("can not read header !");
		}
		
		byte cmd = header[0];
		byte error = header[header.length - 1];
		byte[] lengthBuf = Arrays.copyOfRange(header, 1, header.length - 1);
		int length = Utils.BytesToInt(lengthBuf);
		if(length == 0) {
			System.out.println("no data in ack , just return ...");
			return new HeaderAndData(cmd , error , null);
		}
		byte[] buffer = new byte[length];
		readLen = Utils.readToLength(in, buffer);
		if(readLen <= 0) {
			return null;
		} else if(readLen != length) {
			throw new Exception("can not read data !");
		}
		
		return new HeaderAndData(cmd , error , buffer);
	}
}
