
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
	
}
