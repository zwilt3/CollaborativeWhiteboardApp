import java.net.*;
import java.io.*;

public class FileTransferServer extends Thread{

	public static final int MAX_FILENAME_LEN = 256;

	private ServerSocket serverSocket;

	/**
	 * Opens connections on serverPort to listen for filenames and return their contents.
	 * 
	 * @param serverPort the port to listen on
	 * @throws IOException
	 */
	public FileTransferServer(int serverPort) throws IOException{
		serverSocket = new ServerSocket(serverPort);
		System.out.println("Server is listening on port " + serverPort);

	}

	public void run(){
		while (true){
			Socket clientSocket;
			try {
				System.out.println("Waiting to accept");
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
			System.out.println("Creating a connection");
			this.clientSocket = clientSocket;
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			this.start();
		}
		

		public void run(){
			try{
				System.out.println("Server about to read");
				int filenameLen = input.readInt();
				String filename = IOTools.readFully(input, filenameLen);
				
				System.out.println("Filename = " + filename);

				//Send the bytes
				BufferedReader br = null;
				try{
					br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
				}
				catch(IOException e){
					output.writeBytes("File not found! You requested: " + filename);
					return;
				}
				String line;
				StringBuilder fileData = new StringBuilder();
				while ((line = br.readLine()) != null){
					fileData.append(line);
					System.out.println("line = " + line);
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



}
