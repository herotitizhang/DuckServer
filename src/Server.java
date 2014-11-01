import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


public class Server {
	
	public static DatagramSocket serverSocket = null; 
	public static ChannelManager cm = new ChannelManager();
	
	public static void main (String[] args) {
		
		// setting up
		byte[] receiveData = new byte[1024]; // a placeholder for incoming data
		
		try {
			serverSocket = new DatagramSocket(65533);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		// server starts accepting requests from users and processing them
		while (true) { 
			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				ClientRequest clientRequest = (ClientRequest)Utilities.getObject(receiveData); // Deserialization occurs
				receiveData = new byte[1024]; // TODO the purpose is to clear the placeholder. is it necessary?
				AddressPortPair pair = new AddressPortPair(receivePacket.getAddress(), receivePacket.getPort());
				handleClientRequest(clientRequest, pair);
			} catch (SocketException e) { //there is an error creating or accessing a Socket.
				e.printStackTrace();
			} catch (IOException e) { //there is an error receiving a DatagramPacket.
				e.printStackTrace();
			} 			
			
		}
		
	}
	
	// pair contains the information of a client's IP address and port number.
	public static void handleClientRequest(ClientRequest clientRequest, AddressPortPair pair) {
		
		if (clientRequest.getIdentifier() == 0) { // login request
			HashMap<AddressPortPair, String> common = cm.getChannelMap().get("Common");
			common.put(pair, new String(clientRequest.getUserName()));
			
			/* for debugging: see the content of Common chatroom
			System.out.println("==============================");
			for (Map.Entry<AddressPortPair, String> zu : common.entrySet()) {
				System.out.println("Address:"+ zu.getKey().getAddress());
				System.out.println("Port:"+ zu.getKey().getPort());
				System.out.println("Name:"+ zu.getValue());
				System.out.println();
			}
			*/
		} else if (clientRequest.getIdentifier() == 4) { // say request
			// create a server response
			byte[] message = clientRequest.getText();
			byte[] channelName = clientRequest.getChannelName();
			String unAdjustedUsername =    // first get the active Channel the user is in, then get his/her name in the channel
					cm.getChannelMap().get(new String(channelName)).get(pair);
			byte[] userName = Utilities.fillInByteArray(unAdjustedUsername, 32);
			ServerResponse response = new ServerResponse(message, channelName, userName);
			byte[] dataToBeSent = Utilities.getByteArray(response); // serialization occurs
			
			// send say response to all memebers in the channel
			HashMap<AddressPortPair, String> channel = cm.getChannelMap().get(new String(clientRequest.getChannelName()));
			if (channel != null) {
				for (AddressPortPair pairInChannel: channel.keySet()) {
					DatagramPacket packet = 
							new DatagramPacket(dataToBeSent, dataToBeSent.length, 
									pairInChannel.getAddress(), pairInChannel.getPort());
					try {
						serverSocket.send(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else { // think of a way to handle a channel that does not exist
				
			}
		}
		
	}
}
