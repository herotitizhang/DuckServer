import java.net.InetAddress;
import java.util.Hashtable;

/**
 * ChannelManager is a data structure the server uses to keep track of all 
 * channels and logged-in users in each channel.
 * 
 * A hashtable is used to store <String, Hashtable> entries, where String is the 
 * name of a channel, and Hashtable contains information of users who are 
 * currently in that channel. 
 * 
 * The inner hashtable stores <String, String> entries, where the first String
 * contains information about a user's IP address and port number, and the 
 * second String is the username.
 * 
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class ChannelManager {
	
	private Hashtable<String, Hashtable<String, String>> channelTable;
	private Hashtable<String, String> allUsers;

	public ChannelManager() {
		channelTable = new Hashtable<String, Hashtable<String, String>>();
		allUsers = new Hashtable<String, String>();
		channelTable.put("Common", new Hashtable<String, String>()); // Common channel is created in the beginning
	}
	
	public void createChannel(String channelName) {
		channelTable.put(channelName, new Hashtable<String, String>());
	}
	
	public void deleteChannel(String channelName) {
		if (!channelName.equals("Common")) 
			channelTable.remove(channelName);
	}
	
	public void addUserToChannel(String channelName, String pair, String userName) {
		Hashtable<String, String > channel = channelTable.get(channelName);
		if (channel != null) {
			channel.put(pair, userName);
			if (!allUsers.containsKey(pair)) 
				allUsers.put(pair,userName);
		}
	}
	
	
	public void deleteUserFromChannel(String channelName, String pair){
		Hashtable<String, String > channel = channelTable.get(channelName);
		if (channel != null) {
			channel.remove(pair);
			if (channel.isEmpty()) {
				deleteChannel(channelName);
			}
		}
		
		// note: the user is only deleted in the channel, but he/she is still in allUsers
	}
	
	public Hashtable<String, Hashtable<String, String>> getChannelTable() {
		return channelTable;
	}
	
	public Hashtable<String, String> getAllUsers() {
		return allUsers;
	}

}

