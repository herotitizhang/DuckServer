import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * ChannelManager is a data structure the server uses to keep track of all 
 * channels and logged-in users in each channel.
 * 
 * A hashtable is used to store <String, Channel> entries, where String is the 
 * name of a channel, and Channel contains information of users who are 
 * currently in that channel and servers that are subscribed to the channel.
 * 
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class ChannelManager {
	
	private Hashtable<String, Channel> channelTable;
	
	// indicate all online users. a user may not belong to any existing channel, 
	// e.g., quit common right after logging in. This hashtable keeps track of 
	// all clients that are online, no matter whether they belong to any channel
	private Hashtable<String, String> allUsers; 

	public ChannelManager() {
		channelTable = new Hashtable<String, Channel>();
		allUsers = new Hashtable<String, String>();
		channelTable.put("Common", new Channel()); // Common channel is created in the beginning
	}
	
	public void createChannel(String channelName) {
		channelTable.put(channelName, new Channel());
	}
	
	// should be called after a new channel is created
	public void initializeRoutingTableInChannel(String channelName, ArrayList<AddressPortPair> neighbors){
		channelTable.get(channelName).setRoutingTable(neighbors);
	}
	
	public void deleteChannel(String channelName) {
		if (!channelName.equals("Common")) 
			channelTable.remove(channelName);
	}
	
	public void addUserToChannel(String channelName, String pair, String userName) {
		Hashtable<String, String > clients = channelTable.get(channelName).getClients();
		if (clients != null && pair != null && userName != null) {
			clients.put(pair, userName);
			if (!allUsers.containsKey(pair)) 
				allUsers.put(pair,userName);
		}
	}
	
	
	public void deleteUserFromChannel(String channelName, String pair){
		Hashtable<String, String > clients = channelTable.get(channelName).getClients();
		if (clients != null) {
			clients.remove(pair);
			if (clients.isEmpty()) {
				deleteChannel(channelName);
			}
		}
		
		// note: the user is only deleted in the channel, but he/she is still in allUsers
	}
	
	public Hashtable<String, Channel> getChannelTable() {
		return channelTable;
	}
	
	public Hashtable<String, String> getAllUsers() {
		return allUsers;
	}

}

