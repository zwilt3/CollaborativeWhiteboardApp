import java.net.*;
import java.util.HashMap;
import java.io.*;

public class FileTransferServer extends Thread{

	public static final int MAX_FILENAME_LEN = 256;

	private ServerSocket serverSocket;
	private int serverPort;
	private HashMap<String, String> users;

	/**
	 * Opens connections on serverPort to listen for filenames and return their contents.
	 * 
	 * @param serverPort the port to listen on
	 * @throws IOException
	 */
	public FileTransferServer(int serverPort) throws IOException{
		this.serverPort = serverPort;
		serverSocket = new ServerSocket(serverPort);
		users = fakeUserDatabase();
	}
	
	private HashMap<String, String> fakeUserDatabase(){
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put("user", "pass");
		ret.put("", "");
		return ret;
	}

	public void run(){
		System.out.println("Server is listening on port " + serverPort);
		int i = 0;
		while (true){
			System.out.println("Starting connection " + (++i));
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				new Connection(clientSocket);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Ending connection " + i);
		}
	}

	private class Connection extends Thread{
		DataInputStream input;
		DataOutputStream output;
		Socket clientSocket;

		public Connection(Socket clientSocket) throws IOException, InterruptedException{
			this.clientSocket = clientSocket;
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			
			this.start();
			this.join();
		}
		
		public boolean authenticate() throws IOException{
			System.out.println("//step 1.server - get username");
			int usernameLen = input.readInt();
			String username = IOTools.readFully(input, usernameLen);
			
			System.out.println("//step 2.server - getpassword");
			int passLen = input.readInt();
			String password = IOTools.readFully(input, passLen);
			
			System.out.println("//step 3.server - send \"OK\" or \"FAIL\"");
			boolean isAuth = !users.containsKey(username) || users.get(username).equals(hash(password));
			if (isAuth) sendMessage("OK");
			else sendMessage("FAIL");
			return isAuth;
		}
		
		private String hash(String password){
			return password;
		}
		
		private void sendFailure() throws IOException{
			sendMessage("We have encountered an unknown server error.");
		}
		
		private void sendMessage(String message) throws IOException{
			output.writeInt(message.length());
			output.writeBytes(message);
			output.flush();
		}

		public void run(){
			try{
				
				if (!authenticate()){
					sendFailure();
					return;
				}
				
				//step 4.server - determine operation type
				int operationLen = input.readInt();
				String operation = IOTools.readFully(input, operationLen);
				
				if ("CLIENT_GET_FILE".equals(operation)){//
					//step 5.server - get filename
					int filenameLen = input.readInt();
					String filename = IOTools.readFully(input, filenameLen);
										
					//step 6.server - send file contents
					sendMessage(IOTools.readFile(filename));
				}
				else if ("CLIENT_SEND_FILE".equals(operation)){
					
					System.out.println("//step 5.server - get filename");
					int filenameLen = input.readInt();
					String filename = IOTools.readFully(input, filenameLen);
					
					System.out.println("//step 6.server - get file contents");
					int fileLen = input.readInt();
					String fileData = IOTools.readFully(input, fileLen);					
					System.out.println("Server got file:\n" + fileData + "\nEOF\n\n");

				}
				else{
					sendMessage("[ERROR] Unknown operation.");
					return;
				}
				
				
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException{
		int port = 8009;
		FileTransferServer server = new FileTransferServer(port);
		server.start();
		FileTransferClient client = new FileTransferClient("localhost", port);
		System.out.println(client.getFile("/TestFile"));
	}


}
