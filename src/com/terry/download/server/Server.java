package com.terry.download.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static int port = 12345;
	private static String IP = "10.240.157.116";
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("./server server_port");
		} else {	
			port = Integer.valueOf(args[0]);
		}
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			while(true) {
				Socket client = server.accept();
				System.out.println("connection from " + client.getInetAddress());
				Thread t = new SocketThread(client);
//				t.setDaemon(false);
				t.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
