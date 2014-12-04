import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class S2SRequestGenerator {

	public static byte[] generateS2SJoinMessage(String channelName) {
		// generate S2S join message
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
	
	public static byte[] generateS2SSayMessage(/* TODO add params */) {
		
		
		return null;
	}

	
	private static byte[] getUniqueIdentifiers() {
		
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream("/dev/urandom"); // read a random number
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    
		// Instantiate an array
	    byte []arr= new byte[64];

	    // read All bytes of File stream
	    try {
			fileStream.read(arr,0,64);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return arr;
	}
	
}
