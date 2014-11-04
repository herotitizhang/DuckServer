import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;


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
			
//				/* for debugging: see the content of Common chatroom
//			System.out.println("==============================");
//			for (Map.Entry<String, String> zu : common.entrySet()) {
//				System.out.println("Address: "+ zu.getKey().split(" ")[0]);
//				System.out.println("Port: "+ zu.getKey().split(" ")[1]);
//				System.out.println("Name: "+ zu.getValue());
//				System.out.println();
//			}
//				*/
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
		
		//  leave request.
		else if (clientRequest.getIdentifier() == 3){
			byte[] channelName = clientRequest.getChannelName();
			String name = new String(channelName);
			cm.deleteUserFromChannel(name.trim(), pair);  // delete user from channel based on pair
			
			if(cm.getChannelMap().get(cm).isEmpty()==true)
				cm.deleteChannel(name);	 // if the channel is empty, then delete it.
				
		}
		
	}
}
