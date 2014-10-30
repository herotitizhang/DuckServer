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
 * The inner hashmap stores <AddressPortPair, String> entries, where AddressPortPair
 * is a class that contains a user's IP address and port number, and String is the 
 * username.
 * 
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class ChannelManager {
	
	private HashMap<String, HashMap<AddressPortPair, String>> channelMap;

	public ChannelManager() {
		channelMap = new HashMap<String, HashMap<AddressPortPair, String>>();
		channelMap.put("Common", new HashMap<AddressPortPair, String>()); // Common channel is created in the beginning
	}
	
	public void createChannel(String channelName) {
		channelMap.put(channelName, new HashMap<AddressPortPair, String>());
	}
	
	public void deleteChannel(String channelName) {
		if (channelName.equals("Common")) {
			System.out.println("Can not delete Common!");
		} else {
			channelMap.remove(channelName);
		}
	}
	
	public HashMap<String, HashMap<AddressPortPair, String>> getChannelMap() {
		return channelMap;
	}

}

