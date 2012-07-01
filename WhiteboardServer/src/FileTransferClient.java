import java.net.*;
import java.io.*;

//import com.example.helloworld.PathData;

public class FileTransferClient {

	private Socket socket;
	private static final int BUFFER_LEN = 4096;
	private String username, password;
	
	public FileTransferClient(String serverIP, int serverPort) throws UnknownHostException, IOException{
		this(serverIP, serverPort, "", "");
	}
	
	public FileTransferClient(String serverIP, int serverPort, String username, String password) throws UnknownHostException, IOException{
		socket = new Socket(serverIP, serverPort);
		this.username = username;
		this.password = password;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		FileTransferClient client = new FileTransferClient("98.232.8.251", 8000);
		System.out.println(client.getFile("Hello.txt"));
	}
	
	public void sendMessage(DataOutputStream output, String message) throws IOException{
		output.writeInt(message.length());
		output.writeBytes(message);
		output.flush();
	}
	
	/*public PathData loadPathData(String filename) throws IOException{
		String pathContents = getFile(filename);
		
	}*/
	
	/**
	 * This is step 1 - login
	 * @param input
	 * @param output
	 * @return
	 * @throws IOException 
	 */
	private boolean doLogin(DataInputStream input, DataOutputStream output) throws IOException{
		//step 1.client - send username
		sendMessage(output, username);
		
		//step 2.client - send password
		sendMessage(output, password);
		
		//step 3.client - get login response
		int len = input.readInt();
		String response = IOTools.readFully(input, len);
		return response.equals("OK");
	}
	
	public String getFile(String filename) throws IOException{
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		
		boolean loggedIn = doLogin(input, output);
		if (!loggedIn) return "[ERROR] Could not log in to server";
		
		System.out.println("Client <" + username + ", " + password + "> logged in successfully!");
		
		//step 4.client - send filename
		sendMessage(output, filename);
		
		//step 5.client - get file contents
		int fileLen = input.readInt();
		String fileData = IOTools.readFully(input, fileLen);
		return fileData;
	}
	
}
