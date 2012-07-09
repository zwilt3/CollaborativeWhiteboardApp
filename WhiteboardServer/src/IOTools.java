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
	
	public static String readFile(String filename) throws IOException{
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
		}
		catch(IOException e){
			throw new FileNotFoundException("[ERROR] File not found! You requested: " + filename);
		}
		String line;
		StringBuilder fileData = new StringBuilder();
		while ((line = br.readLine()) != null){
			fileData.append(line);
		}
		return fileData.toString();
	}


}
