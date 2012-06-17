import java.net.*;
import java.io.*;

public class FileTransferServer extends Thread{

	public static final int MAX_FILENAME_LEN = 256;

	private ServerSocket serverSocket;
	private int serverPort;

	/**
	 * Opens connections on serverPort to listen for filenames and return their contents.
	 * 
	 * @param serverPort the port to listen on
	 * @throws IOException
	 */
	public FileTransferServer(int serverPort) throws IOException{
		this.serverPort = serverPort;
		serverSocket = new ServerSocket(serverPort);
	}

	public void run(){
		System.out.println("Server is listening on port " + serverPort);
		while (true){
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				new Connection(clientSocket);
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class Connection extends Thread{
		DataInputStream input;
		DataOutputStream output;
		Socket clientSocket;

		public Connection(Socket clientSocket) throws IOException{
			this.clientSocket = clientSocket;
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			this.start();
		}
		

		public void run(){
			try{
				int filenameLen = input.readInt();
				String filename = IOTools.readFully(input, filenameLen);
				
				//Send the bytes
				BufferedReader br = null;
				try{
					br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
				}
				catch(IOException e){
					String errorMessage = "File not found! You requested: " + filename;
					output.writeInt(errorMessage.length());
					output.writeBytes(errorMessage);
					return;
				}
				String line;
				StringBuilder fileData = new StringBuilder();
				while ((line = br.readLine()) != null){
					fileData.append(line);
				}
				output.writeInt(fileData.length());
				output.writeBytes(fileData.toString());
				output.flush();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException{
		FileTransferServer server = new FileTransferServer(8008);
		server.start();
	}


}
