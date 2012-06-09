import java.net.*;
import java.io.*;

public class FileTransferClient {

	private Socket socket;
	private static final int BUFFER_LEN = 4096;
	
	public FileTransferClient(String serverIP, int serverPort) throws UnknownHostException, IOException{
		socket = new Socket(serverIP, serverPort);
		
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		FileTransferClient client = new FileTransferClient("192.168.5.212", 8000);
		System.out.println(client.getFile("/TestFile"));
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
	
}
