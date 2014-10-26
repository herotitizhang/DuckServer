import java.net.InetAddress;
import java.util.HashMap;

/**
 * ChatroomManager is a data structure the server uses to keep track of all 
 * chatrooms and logged-in users in each chatroom. (A chatroom is a channel)
 * 
 * A hashmap is used to store <String, HashMap> entries, where String is the 
 * name of a chatroom, and HashMap contains information of users who are 
 * currently in that chatroom. 
 * 
 * The inner hashmap stores <AddressPortPair, String> entries, where AddressPortPair
 * is a class that contains a user's IP address and port number, and String is the 
 * username.
 * 
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class ChatroomManager {
	
	HashMap<String, HashMap<String, String>> chatroomMap;
	
	public ChatroomManager() {
		chatroomMap = new HashMap<String, HashMap<String, String>>();
		chatroomMap.put("Common", new HashMap<String, String>()); // Common chatroom is created in the beginning
	}
	
	public void createChatroom(String chatroomName) {
		chatroomMap.put(chatroomName, new HashMap<String, String>());
	}
	
	public void deleteChatroom(String chatroomName) {
		if (chatroomName.equals("Common")) {
			System.out.println("Can not delete Common!");
		} else {
			chatroomMap.remove(chatroomName);
		}
	}

}

