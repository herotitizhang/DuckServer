import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class S2SRequestGenerator {

	public static byte[] generateS2SJoinMessage(String channelName) {

		byte[] identifier = new byte[4];
	    identifier[0] = 8;
	    
	    byte[] cName = Utilities.fillInByteArray(channelName, 32);
	    
	    if (cName.length == 0) return null;

    	byte[] request = new byte[4+32];
    	for (int i = 0; i < 4; i++) {
    		request[i] = identifier[i]; 
    	}
    	for (int i = 4; i < 36; i++) {
    		request[i] = cName[i-4];
    	}
    	
    	return request;
	}
	
	public static byte[] generateS2SLeaveMessage(String channelName) {
	    
		byte[] identifier = new byte[4];
	    identifier[0] = 9;
	    
	    byte[] cName = Utilities.fillInByteArray(channelName, 32);
	    
	    if (cName.length == 0) return null;

    	byte[] request = new byte[4+32];
    	for (int i = 0; i < 4; i++) {
    		request[i] = identifier[i]; 
    	}
    	for (int i = 4; i < 36; i++) {
    		request[i] = cName[i-4];
    	}
    	return request;
	    
	}
	
	public static byte[] generateS2SSayMessage(String userName, String channelName, String textField)  {
		
		byte[] identifier = new byte[4];
	    identifier[0] = 10;
		
	    byte[] uniqueId = getUniqueIdentifiers();
	    
	    byte[] uName = Utilities.fillInByteArray(userName, 32);
	    
	    byte[] cName = Utilities.fillInByteArray(channelName, 32);
	    
	    byte[] tField = Utilities.fillInByteArray(textField, 64);
	    
	    byte[] request = new byte[4+8+32+32+64];
	    
	    for (int i = 0; i < 4; i++) {
    		request[i] = identifier[i]; 
    	}
    	for (int i = 4; i < 12; i++) {
    		request[i] = uniqueId[i-4];
    	}
    	for (int i = 12; i < 44; i++) {
    		request[i] = uName[i-12]; 
    	}
    	for (int i = 44; i < 76; i++) {
    		request[i] = cName[i-44]; 
    	}
    	for (int i = 76; i < 140; i++) {
    		request[i] = tField[i-76]; 
    	}
    	
	    
	    return request;
	}
	
	public static byte[] generateS2SSayMessage(byte[] userName, byte[] channelName, byte[] textField)  {
		ArrayList<byte[]> toBeCombined = new ArrayList<byte[]>();
		byte[] identifier = new byte[4];
	    identifier[0] = 10;
	    toBeCombined.add(identifier);
		
	    byte[] uniqueId = getUniqueIdentifiers();
	    toBeCombined.add(uniqueId);
	    
	    toBeCombined.add(userName);
	    toBeCombined.add(channelName);
	    toBeCombined.add(textField);
	    
	    return Utilities.combineListOfByteArrays(toBeCombined);
	    
	    
	    
	}


	
	private static byte[] getUniqueIdentifiers() {
		
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream("/dev/urandom"); // read a random number
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    
		// Instantiate an array
	    byte []arr= new byte[8];

	    // read All bytes of File stream
	    try {
			fileStream.read(arr,0,8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return arr;
	}
	
}
