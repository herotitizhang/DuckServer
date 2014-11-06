import java.util.ArrayList;

/**
 * Utilities class contains methods needed by different classes.
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class Utilities {
	
	 
	/**
	 * We want the fields in a ClientRequest to have the exact sizes specified in 
	 * the DuckChat protocol. For example, a user name has to be 32 bytes, not more
	 * or less.
	 * 
	 * @param str: the input String that needs to be filled in the byte array
	 * @param size: the size of the byte array that will be returned
	 * @return a byte array with the size specified 
	 */
	public static byte[] fillInByteArray(String str, int size) {
		byte[] wellSized = new byte[size];
		byte[] content = str.getBytes();
		int iterationNum = (size > content.length) ? content.length : size;
		for (int i = 0; i < iterationNum; i++) {
			wellSized[i] = content[i];
		}
		return wellSized;
		
	}
	
	/**
	 * get a list of byte arrays and combine them
	 * @param byteArrays
	 * @return a byte array
	 */
	public static byte[] combineListOfByteArrays(ArrayList<byte[]> byteArrays) {
		
		int length = 0;
		for (int i = 0; i < byteArrays.size(); i++) length += byteArrays.get(i).length;
		byte[] combination = new byte[length];
		
		int pointer = 0;
		for (int i = 0; i < byteArrays.size(); i++) {
			for (int j = 0; j < byteArrays.get(i).length; j++, pointer++) {
				combination[pointer] = byteArrays.get(i)[j];
			}
		}
		
		return combination;
	}
	
	
	
}
