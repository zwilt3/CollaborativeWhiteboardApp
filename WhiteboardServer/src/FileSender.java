import java.net.*;
import java.io.*;

public class FileSender {
	
	public void sendFile(Socket socket, String filename) throws IOException{
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		
		StringBuilder data = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
		String line;
		while ((line = br.readLine()) != null){
			data.append(line);
		}
		output.writeBytes(data.toString());
	}
	
	
	
	
}
