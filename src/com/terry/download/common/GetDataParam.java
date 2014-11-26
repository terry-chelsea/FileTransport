package com.terry.download.common;

public class GetDataParam {
	private String filepath = null;
	private long offset = 0;
	private long length  = 0;
	
	public GetDataParam() {
		
	}
	
	public GetDataParam(String path , long off , long len) {
		this.filepath = path;
		this.offset = off;
		this.length = len;
	}
	
	public void setFilepath(String path) {
		this.filepath = path;
	}
	
	public void setOffset(long off) {
		this.offset = off;
	}
	
	public void setLength(long len) {
		this.length = len;
	}
	
	public String getFilepath() {
		return this.filepath;
	}
	
	public long getOffset() {
		return this.offset;
	}
	
	public long getLength() {
		return this.length;
	}
	
	public String toString() {
		return "filename : " + filepath + " offset : " + offset + " length : " + length;
	}
}
