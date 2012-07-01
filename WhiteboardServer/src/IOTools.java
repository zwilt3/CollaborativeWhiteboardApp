import java.io.*;
public class IOTools {
	
	private static final int BUFFER_LEN = 4096;
	
	public static String readFully(InputStream input, int totalLen) throws IOException{
		byte[] bytes = new byte[totalLen];
		for (int i = 0; i < totalLen; i++){
			bytes[i] = (byte)input.read();
		}
		return new String(bytes);
	}


}
