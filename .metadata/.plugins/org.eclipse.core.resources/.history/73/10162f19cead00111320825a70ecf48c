import java.io.*;
public class SerializationTest {
	
	private static class TestClass implements Serializable{
		String str;
		int num;
		int num2;
		int num3;
		int[] arr;
		public TestClass(String str, int num, int num2, int... arr){
			this.str = str;
			this.num = num;
			this.num2 = num2;
			this.arr = arr;
			for (int i = 1; i < arr.length; i++){
				arr[i] = arr[i-1] + arr[i];
			}
		}
		
		public String toString(){
			String ret = "<String: " + str + ", Number: " + num + ">";
			for (int x : arr){
				ret += " " + x;
			}
			return ret;
		}
	}

	public static void main(String[] args){
		
		//Serialization
		try{
			TestClass tc1 = new TestClass("Hello, World!1111111111", 42, 314, 1,2,3,4);
			System.out.println("tc1 = " + tc1);
			FileOutputStream fos = new FileOutputStream("SavedTC1");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(tc1);
			oos.flush();
			oos.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		//Deserialization
		try{
			FileInputStream fis = new FileInputStream("SavedTC1");
			ObjectInputStream ois = new ObjectInputStream(fis);
			TestClass tc2 = (TestClass)ois.readObject();
			ois.close();
			System.out.println("tc2 = " + tc2);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
