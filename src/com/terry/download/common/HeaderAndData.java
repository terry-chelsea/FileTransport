package com.terry.download.common;

public class HeaderAndData {
	private byte cmd;
	private byte error;
	private byte[] data;
	
	public HeaderAndData() {
		
	}
	
	public HeaderAndData(byte cmd , byte error , byte[] data) {
		this.cmd = cmd;
		this.error = error;
		this.data = data;
	}
	
	public void setCmd(byte cmd) {
		this.cmd = cmd;
	}
	
	public void setError(byte err) {
		this.error = err;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte getCmd() {
		return this.cmd;
	}
	
	public byte getError() {
		return this.error;
	}
	
	public byte[] getData() {
		return this.data;
	}
}
