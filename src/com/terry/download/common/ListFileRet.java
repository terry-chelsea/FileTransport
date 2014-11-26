package com.terry.download.common;

public class ListFileRet {
	private String name = null;
	private int type = 0;
	
	public ListFileRet() {
		
	}
	
	public ListFileRet(String name , int type) {
		this.name = name;
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(int t) {
		this.type = t;
	}
	
	public int getType() {
		return this.type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		String tp = "undefined";
		if(this.type == 0)
			tp = "file";
		else if(this.type == 1) 
			tp = "directory";
		return "name : " + this.name + "==============" + "type : " + tp;
	}
 }
