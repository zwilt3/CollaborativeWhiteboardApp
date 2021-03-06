package com.example.helloworld;

import java.net.*;
import java.io.*;

public class FileTransferClient {

	private Socket socket;
	private static final int BUFFER_LEN = 4096;
	
	
	public static void main (String[] args) throws UnknownHostException, IOException
	{
		System.out.println("hi");
		FileTransferClient client =	new FileTransferClient("76.121.49.1",8000);
		System.out.println(client.getFile("Hello.txt"));
	
	}
	
	public FileTransferClient(String serverIP, int serverPort) throws UnknownHostException, IOException{
		socket = new Socket(serverIP, serverPort);
		
	}
	
	public String getFile(String filename) throws IOException{
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		output.writeInt(filename.length());//send the length, so we know when to stop
		output.writeBytes(filename);//send the filename to the server
		output.flush();
		int fileLen = input.readInt();
		String fileData = IOTools.readFully(input, fileLen);
		return fileData;
	}
	
	public int sendPathData(PathData pData)
	{
		return 0;
	}
	/*public FileTransferClient(String serverIP, int serverPort, Serializable object) throws UnknownHostException, IOException{
		socket = new Socket(serverIP, serverPort);
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		output.writeInt(filename.length());//send the length, so we know when to stop
		output.writeBytes(filename);//send the filename to the server
		System.out.println("I wrote: " + filename);
		output.flush();
		int fileLen = input.readInt();
		String fileData = IOTools.readFully(input, fileLen);
		System.out.println("The file I read was:\n" + fileData);
	}*/
	
	
	
}
