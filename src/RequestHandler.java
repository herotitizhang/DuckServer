import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
		} else if (clientRequest.getIdentifier() == 5) { // list request
			Map m = Collections.synchronizedMap(cm.getChannelMap()); // not sure if it's correct usage
			synchronized (m){
				int numOfChannels = m.size();
				byte[][] channelList = new byte[numOfChannels][];
				//TO DO finish it
			};
			
			
			
			
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
