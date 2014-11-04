import java.net.InetAddress;
import java.util.HashMap;

/**
 * ChannelManager is a data structure the server uses to keep track of all 
 * channels and logged-in users in each channel.
 * 
 * A hashmap is used to store <String, HashMap> entries, where String is the 
 * name of a channel, and HashMap contains information of users who are 
 * currently in that channel. 
 * 
 * The inner hashmap stores <String, String> entries, where the first String
 * contains information about a user's IP address and port number, and the 
 * second String is the username.
 * 
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class ChannelManager {
	
	private HashMap<String, HashMap<String, String>> channelMap;
	private HashMap<String, String> allUsers;

	public ChannelManager() {
		channelMap = new HashMap<String, HashMap<String, String>>();
		allUsers = new HashMap<String, String>();
		channelMap.put("Common", new HashMap<String, String>()); // Common channel is created in the beginning
	}
	
	public void createChannel(String channelName) {
		channelMap.put(channelName, new HashMap<String, String>());
	}
	
	public void deleteChannel(String channelName) {
		if (channelName.equals("Common")) {
			System.out.println("Can not delete Common!");
		} else {
			channelMap.remove(channelName);
		}
	}
	
	public void addUserToChannel(String channelName, String pair, String userName) {
		HashMap<String, String > channel = channelMap.get(channelName);
		if (channel != null) {
			channel.put(pair, userName);
			if (!allUsers.containsKey(pair)) 
				allUsers.put(pair,userName);
		}
	}
	
	
	public void deleteUserFromChannel(String channelName, String pair){
		HashMap<String, String > channel = channelMap.get(channelName);
		if (channel != null) channel.remove(pair); 
		// note: the user is only deleted in the channel, but he/she is still in allUsers
	}
	
	public HashMap<String, HashMap<String, String>> getChannelMap() {
		return channelMap;
	}
	
	public HashMap<String, String> getAllUsers() {
		return allUsers;
	}

}

