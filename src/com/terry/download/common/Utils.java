package com.terry.download.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Utils {
	public final static int PRE_HEAD_LENGTH = Integer.SIZE / 8;
	public static byte[] IntegerToBytes(int a) {
//		assert(a.SIZE % 8 == 0);
		int len = Integer.SIZE / 8;
		byte[] bytes = new byte[len];
		for(int i = 0 ; i < len ; ++ i) {
			int bits = i * 8;
			bytes[i] = (byte) ((a >> bits) & 0xFF);
		}
		
		return bytes;
	}

	public static int BytesToInt(byte[] bytes) {
		int value = 0;
		int len = bytes.length > Integer.SIZE / 8  ? Integer.SIZE / 8 : bytes.length;
		for(int i = 0 ; i < len ; ++ i) {
			int bits = i * 8;
			int to = (int) (bytes[i] << bits);
			value |= to;
			if(value < 0) {
				int newBits = bits + 8;
				if(newBits > Integer.SIZE)
					continue ;
				value ^= 0xFF << newBits;
			}
		}
		
		return value;
	}
	
	public static int readToLength(InputStream in , byte[] buffer) {
		int length = buffer.length;
		int readn = 0;
		int left = length;
		while(left > 0) {
			int len = 0;
			try {
				len = in.read(buffer, readn, left);
				if(len <= 0)
					return -1;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return readn;
			}
			if(len <= 0) {
				return readn;
			} else {
				readn += len;
				left -= len;
			}
		}
		return length;
	}
	
	public static boolean check(byte[] buffer , String source , int times) {
		StringBuffer buf = new StringBuffer();
		for(int i = 0 ; i < times ; ++ i) {
			buf.append(source);
		}
		
		String getBuffer = new String(buffer , Charset.forName("UTF-8"));
		return getBuffer.equals(buf.toString());
	}
	
}
