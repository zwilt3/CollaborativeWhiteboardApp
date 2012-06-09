import java.io.IOException;
import java.net.UnknownHostException;


public class FileTransferTest {
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		int port = 8002;
		FileTransferServer server = new FileTransferServer(port);
		server.start();
		FileTransferClient client = new FileTransferClient("localhost", port);
		System.out.println(client.getFile("/TestFile"));
	}

}
