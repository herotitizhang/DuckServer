import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class RequestHandler implements Runnable {

	ChannelManager cm;
	DatagramSocket serverSocket;
	ClientRequest clientRequest; 
	String pair;
	
	public RequestHandler(ChannelManager cm, DatagramSocket serverSocket, 
			ClientRequest clientRequest, String pair) {
		this.cm = cm;
		this.serverSocket = serverSocket;
		this.clientRequest = clientRequest;
		this.pair = pair;
	}
	
	@Override
	public void run() {
		handleClientRequest();
	}

	private void handleClientRequest() {
		
		if (clientRequest.getIdentifier() == 0) { // login request
			HashMap<String, String> common = cm.getChannelMap().get("Common");
			common.put(pair, new String(clientRequest.getUserName()));
		} else if (clientRequest.getIdentifier() == 1) { // logout request
			for(Iterator<Entry<String, HashMap<String, String>>> outerIterator = cm.getChannelMap().entrySet().iterator(); outerIterator.hasNext(); ) {
				Map.Entry<String, HashMap<String, String>> entry = outerIterator.next();
				HashMap<String, String> channel = entry.getValue();
				channel.remove(pair); // we don't need to check if the key-value pair exists since the map will return null if it doesn't.
			}
		} else if (clientRequest.getIdentifier() == 4) { // say request
			// create a server response
			byte[] message = clientRequest.getText();
			byte[] channelName = clientRequest.getChannelName();
			String unAdjustedUsername =    // first get the active Channel the user is in, then get his/her name in the channel
					cm.getChannelMap().get(new String(channelName).trim()).get(pair);
			byte[] userName = Utilities.fillInByteArray(unAdjustedUsername, 32);
			ServerResponse response = new ServerResponse(channelName, userName, message);
			byte[] dataToBeSent = Utilities.getByteArray(response); // serialization occurs
			
			// send say response to all members in the channel
			HashMap<String, String> channel = cm.getChannelMap().get(new String(clientRequest.getChannelName()).trim());
			if (channel != null) {
				for (String pairInChannel: channel.keySet()) {
					try {
						InetAddress destIPAddress = InetAddress.getByName(pairInChannel.split(" ")[0]);
						int destPort = Integer.parseInt(pairInChannel.split(" ")[1]);
						DatagramPacket packet = 
								new DatagramPacket(dataToBeSent, dataToBeSent.length, 
										destIPAddress, destPort);
						serverSocket.send(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else { // think of a way to handle a channel that does not exist
				
			}
			
		} else if (clientRequest.getIdentifier() == 5) { // list request
			Map<String, HashMap<String,String>> m = Collections.synchronizedMap(cm.getChannelMap()); // not sure if it's correct usage
			byte[][] channelList = null;
			ServerResponse serverResponse = null;
			synchronized (m){
				channelList = new byte[m.size()][];
				int i = 0;
				for (String channelName: m.keySet()) {
					channelList[i] = Utilities.fillInByteArray(channelName, 32);
					i++;
				}
				serverResponse = new ServerResponse(m.size(), channelList);
			};
			
			try {
				byte[] dataToBeSent = Utilities.getByteArray(serverResponse); // serialization occurs
				InetAddress destIPAddress = InetAddress.getByName(pair.split(" ")[0]);
				int destPort = Integer.parseInt(pair.split(" ")[1]);
				DatagramPacket packet = 
						new DatagramPacket(dataToBeSent, dataToBeSent.length, 
								destIPAddress, destPort);
				serverSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (clientRequest.getIdentifier() == 6) { // who request
			String channelName = new String(clientRequest.getChannelName()).trim();
			Map<String,String> m = Collections.synchronizedMap(cm.getChannelMap().get(channelName)); // not sure if it's correct usage
			byte[][] nameList = null;
			ServerResponse serverResponse = null;
			synchronized (m){
				nameList = new byte[m.size()][];
				int i = 0;
				for (String userName: m.values()) {
					nameList[i] = Utilities.fillInByteArray(userName, 32);
					i++;
				}
				serverResponse = new ServerResponse(m.size(), 
						Utilities.fillInByteArray(channelName, 32), nameList);
			};		
			
			try {
				byte[] dataToBeSent = Utilities.getByteArray(serverResponse); // serialization occurs
				InetAddress destIPAddress = InetAddress.getByName(pair.split(" ")[0]);
				int destPort = Integer.parseInt(pair.split(" ")[1]);
				DatagramPacket packet = 
						new DatagramPacket(dataToBeSent, dataToBeSent.length, 
								destIPAddress, destPort);
				serverSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//  leave request.
		else if (clientRequest.getIdentifier() == 3){
			byte[] channelName = clientRequest.getChannelName();
			String cName = new String(channelName);
			cm.deleteUserFromChannel(cName.trim(), pair);  // delete user from channel based on pair
			
			if(cm.getChannelMap().get(cName).isEmpty()==true)
				cm.deleteChannel(cName);	 // if the channel is empty, then delete it.
				
		}
		
		// join request
		else if (clientRequest.getIdentifier() == 2){
			String usname = new String(clientRequest.getUserName());
			byte[] channelName = clientRequest.getChannelName();
			String cName = new String(channelName);
			
			//check if the requested join channel exists
			if(cm.getChannelMap().get(cName).containsKey(cName) ==false )
				cm.createChannel(cName); //create
			
			cm.getChannelMap().get(cName).put(pair, usname);
		}
		
	}
	
	private void printAllChannelsAndMembers() {
		for (Map.Entry<String, HashMap<String, String>> channelPair: cm.getChannelMap().entrySet()) {
			System.out.println("==========="+channelPair.getKey()+"============");
			for (Map.Entry<String, String> zu : channelPair.getValue().entrySet()) {
				System.out.println("Address: "+ zu.getKey().split(" ")[0]);
				System.out.println("Port: "+ zu.getKey().split(" ")[1]);
				System.out.println("Name: "+ zu.getValue());
				System.out.println("--------------");
			}
		}
	}
}
