package com.terry.download.common;

import java.io.File;

public class Test {
	public static void main(String[] args) {
		String dir = "\\E:\\terry";
		String[] ps = dir.split("\\\\");
		System.out.println(ps[ps.length - 1]);
		String format = String.format("download (%.2f %%) : %.2f MB of %.2f MB" , 4354343 / (double) 132324344 , 
				11288332 /(double) (1000 * 1000) , 475783478 /(double) (1000 * 1000));
		System.out.println(format);
		File fp = new File(dir);
		System.out.println(fp);
		for(String path : fp.list()) {
			System.out.println(path);
		}
	}
}
