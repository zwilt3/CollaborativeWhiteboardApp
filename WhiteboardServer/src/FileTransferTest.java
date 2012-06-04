import java.io.IOException;
import java.net.UnknownHostException;


public class FileTransferTest {
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		int port = 8017;
		FileTransferServer fts = new FileTransferServer(port);
		fts.start();
		FileTransferClient ftc = new FileTransferClient("localhost", port, "/TestFile");
	}

}
