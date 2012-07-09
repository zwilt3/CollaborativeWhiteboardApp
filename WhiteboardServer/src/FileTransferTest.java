import java.io.IOException;
import java.net.UnknownHostException;


public class FileTransferTest {
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		System.out.println("hi");
		int port = 8018;
		FileTransferServer server = new FileTransferServer(port);
		server.start();
		FileTransferClient client = new FileTransferClient("localhost", port);
		System.out.println("client saw " + client.getFile("/TestFile"));
		client = new FileTransferClient("localhost", port);
		client.sendUpdate("/TestFile");
		System.out.println("client sent ");
	}

}
