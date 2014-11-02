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

	public ChannelManager() {
		channelMap = new HashMap<String, HashMap<String, String>>();
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
	
	public HashMap<String, HashMap<String, String>> getChannelMap() {
		return channelMap;
	}

}

