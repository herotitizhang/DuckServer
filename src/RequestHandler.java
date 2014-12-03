import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class RequestHandler implements Runnable {

	private ChannelManager cm;
	private DatagramSocket serverSocket;
	private byte[] receivedRequest; 
	private String pair;
	private ArrayList<AddressPortPair> neighbors; // no member should be added to or deleted from this ArrayList
	
	public RequestHandler(ChannelManager cm, DatagramSocket serverSocket, 
			byte[] clientRequest, String pair, ArrayList<AddressPortPair> neighbors) {
		this.cm = cm;
		this.serverSocket = serverSocket;
		this.receivedRequest = clientRequest;
		this.pair = pair;
		this.neighbors = neighbors;
	}
	
	@Override
	public void run() {
		if (receivedRequest[0] == 0) { // login request
			handleLoginRequest();
		} else if (receivedRequest[0] == 1) { // logout request
			handleLogoutRequest();
		} else if (receivedRequest[0] == 2){ // join request
			handleJoinRequest();
		} else if (receivedRequest[0] == 3){ // leave request
			handleLeaveRequest();
		} else if (receivedRequest[0] == 4) { // say request
			handleSayRequest();
		} else if (receivedRequest[0] == 5) { // list request
			handleListRequest();
		} else if (receivedRequest[0] == 6) { // who request
			handleWhoRequest();
		} else if (receivedRequest[0] == 8) { // S2S join request
			handleS2SJoinRequest();
			// TODO implements soft-state Join
		} else if (receivedRequest[0] == 9) { // S2S leave request
			handleS2SLeaveRequest();
			// TODO tells another server that it is not taking any request from it
		} else if (receivedRequest[0] == 10) { // S2S say request
			handleS2SSayRequest();
			// TODO add a method that deletes unnecessary servers and forms a real tree without loops
			// this method may call forwardMessage(), which is called in handleJoinRequest
		}
		
	}

	private void handleLoginRequest() {
		
		printAllChannelsAndMembers();
		
		// get channel name
		int lastByteOfUserName;
		for (lastByteOfUserName = 4; lastByteOfUserName < 36; lastByteOfUserName++) {
			if (receivedRequest[lastByteOfUserName] == 0)
				break;
		}
		if (lastByteOfUserName == 36)
			lastByteOfUserName--;
		lastByteOfUserName--;

		byte[] userName = new byte[lastByteOfUserName - 4 + 1];
		for (int i = 4; i <= lastByteOfUserName; i++) {
			userName[i - 4] = receivedRequest[i];
		}

		// add the user to the channel
		cm.getAllUsers().put(pair, new String(userName));
		System.out.println(new String(userName) + " logged in.");
	}
	
	private void handleLogoutRequest() {
		String username = cm.getAllUsers().get(pair);
		cm.getAllUsers().remove(pair);
		for(Iterator<Entry<String, Channel>> outerIterator = cm.getChannelTable().entrySet().iterator(); outerIterator.hasNext(); ) {
			Map.Entry<String, Channel> entry = outerIterator.next();
			Hashtable<String, String> channel = entry.getValue().getClients();
			channel.remove(pair); // we don't need to check if the key-value pair exists since the table will return null if it doesn't.
			if (channel.size() == 0) outerIterator.remove();
		}
		System.out.println(username + " logged out.");

	}
	
	private void handleJoinRequest() {

		// get username
		String userName = cm.getAllUsers().get(pair);
		
		if (userName == null) { // the join request arrives sooner than the login request
			                    // so userName does not exist in the server yet
			try {
				Thread.sleep(2000);
				userName = cm.getAllUsers().get(pair); // get the pair again
				if (userName == null) {
					sendErrorMessage("The user didn't join properly!");
					return;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// get channel name
		int lastByteOfchannelName;
		for (lastByteOfchannelName = 4; lastByteOfchannelName < 36; lastByteOfchannelName++ ){
			if (receivedRequest[lastByteOfchannelName] == 0) break;
		}
		if (lastByteOfchannelName == 36) lastByteOfchannelName --;
		lastByteOfchannelName --;
		
		byte[] channelName = new byte[lastByteOfchannelName-4+1];
		for (int i = 4; i <= lastByteOfchannelName; i++) {
			channelName[i-4] = receivedRequest[i]; 
		}
		
		String cName = new String(channelName);
		
		//check if the requested join channel exists
		if( !cm.getChannelTable().containsKey(cName) ||
				!cm.getChannelTable().get("Common").getClients().containsKey(pair)){
			cm.createChannel(cName); //create
			cm.initializeRoutingTableInChannel(cName,neighbors);
		}
		
		System.out.println("pair = "+pair+", userName = "+userName);
		
		cm.addUserToChannel(cName, pair, userName);
		System.out.println(userName+" joined channel "+cName+".");
		
		
		
		// send join message to servers
		byte[] request = S2SRequestGenerator.generateS2SJoinMessage(cName);
    	
		ArrayList<AddressPortPair> receivingServers = cm.getChannelTable().get(cName).getRoutingTable();
		for (AddressPortPair receivingServer: receivingServers) {
			
	    	try {
		    	// send S2S join message
		    	DatagramPacket packet = 
						new DatagramPacket(request, request.length, 
								receivingServer.getAddress(), receivingServer.getPort());
		   		serverSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	// print send prompt
			StringBuilder prompt = new StringBuilder();
			prompt.append(serverSocket.getLocalSocketAddress()).append(" ");
			String receiverAddress = receivingServer.getAddress().toString()
					.replace("localhost", "");
			System.out.println("yoyo!"+receiverAddress);
			prompt.append(receiverAddress).append(":").append(receivingServer.getPort()).append(" ");
			prompt.append("send S2S Join ").append(cName);
			System.out.println(prompt);
	    	
		}
		

		
	}
	
	private void handleLeaveRequest() {

		// get channel name
		int lastByteOfchannelName;
		for (lastByteOfchannelName = 4; lastByteOfchannelName < 36; lastByteOfchannelName++) {
			if (receivedRequest[lastByteOfchannelName] == 0)
				break;
		}
		if (lastByteOfchannelName == 36)
			lastByteOfchannelName--;
		lastByteOfchannelName--;

		byte[] channelName = new byte[lastByteOfchannelName - 4 + 1];
		for (int i = 4; i <= lastByteOfchannelName; i++) {
			channelName[i - 4] = receivedRequest[i];
		}

		// delete the user from the channel
		cm.deleteUserFromChannel(new String(channelName), pair); 
		System.out.println(cm.getAllUsers().get(pair)+" left channel "+new String(channelName));
	}
	
	private void handleSayRequest() {
		
		ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();

		// create identifier
		byte[] identifier = new byte[4];
		identifier[0] = 0;
		
		// create channelName
		int lastByteOfChannelName;
		for (lastByteOfChannelName = 4; lastByteOfChannelName < 36; lastByteOfChannelName++ ){
			if (receivedRequest[lastByteOfChannelName] == 0) break;
		}
		if (lastByteOfChannelName == 36) lastByteOfChannelName --;
		lastByteOfChannelName --;
		
		byte[] channelName = new byte[32];
		for (int i = 4; i <= lastByteOfChannelName; i++) {
			channelName[i-4] = receivedRequest[i]; 
		}
		
		// create userName
		String uName = cm.getAllUsers().get(pair);
		byte[] userName = Utilities.fillInByteArray(uName, 32);
		
		// create textField
		int lastByteOfTextField;
		for (lastByteOfTextField = 36; lastByteOfTextField < 100; lastByteOfTextField++ ){
			if (receivedRequest[lastByteOfTextField] == 0) break;
		}
		if (lastByteOfTextField == 100) lastByteOfTextField --;
		lastByteOfTextField --;
		
		byte[] textField = new byte[64];
		for (int i = 36; i <= lastByteOfTextField; i++) {
			textField[i-36] = receivedRequest[i]; 
		}
		
		// combine the byte arrays
		byteArrays.add(identifier);
		byteArrays.add(channelName);
		byteArrays.add(userName);
		byteArrays.add(textField);
		
		byte[] dataToBeSent = Utilities.combineListOfByteArrays(byteArrays);


		// send say response to all members in the channel
		Channel channel = cm.getChannelTable().get(new String(channelName).trim());
		if (channel != null) {
			for (String pairInChannel : channel.getClients().keySet()) {
				try {
					InetAddress destIPAddress = InetAddress
							.getByName(pairInChannel.split(" ")[0]);
					int destPort = Integer
							.parseInt(pairInChannel.split(" ")[1]);
					DatagramPacket packet = new DatagramPacket(dataToBeSent,
							dataToBeSent.length, destIPAddress, destPort);
					serverSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else { // think of a way to handle a channel that does not exist

		}
		
		System.out.println(uName+" said something in channel "+new String(channelName).trim());
	}
	
	private void handleListRequest() {
		ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();
		
		Hashtable<String, Channel> table = cm.getChannelTable();
		
		// create identifier
		byte[] identifier = new byte[4];
		identifier[0] = 1;
		
		// create numOfChannels
		byte[] numOfChannels = new byte[4];
		numOfChannels[0] = (byte)table.size();
		
		byteArrays.add(identifier);
		byteArrays.add(numOfChannels);
		
		for (String channelName: table.keySet()) {
			byteArrays.add(Utilities.fillInByteArray(channelName, 32));
		}
		
		try {
			byte[] dataToBeSent = Utilities.combineListOfByteArrays(byteArrays);
			InetAddress destIPAddress = InetAddress.getByName(pair.split(" ")[0]);
			int destPort = Integer.parseInt(pair.split(" ")[1]);
			DatagramPacket packet = 
					new DatagramPacket(dataToBeSent, dataToBeSent.length, 
							destIPAddress, destPort);
			serverSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(cm.getAllUsers().get(pair)+" sent a list request.");
	}

		
	private void handleWhoRequest() {
		
		// get channelName
		int lastByteOfChannelName;
		for (lastByteOfChannelName = 4; lastByteOfChannelName < 36; lastByteOfChannelName++ ){
			if (receivedRequest[lastByteOfChannelName] == 0) break;
		}
		if (lastByteOfChannelName == 36) lastByteOfChannelName --;
		lastByteOfChannelName --;
		
		byte[] channelName = new byte[32];
		for (int i = 4; i <= lastByteOfChannelName; i++) {
			channelName[i-4] = receivedRequest[i]; 
		}
		
		// create byte array
		ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();
		
		// create identifier
		byte[] identifier = new byte[4];
		identifier[0] = 2;
		byteArrays.add(identifier);
		
		Channel channel = cm.getChannelTable().get(new String(channelName).trim());
		
		if (channel == null) {
			sendErrorMessage("The channel does not exist!");
			return;
		}
		
		// create numOfChannels
		byte[] numOfChannels = new byte[4];
		numOfChannels[0] = (byte)channel.getClients().size();
		
		byteArrays.add(numOfChannels);
		byteArrays.add(channelName);
		
		for (String userName: channel.getClients().values()) {
			byteArrays.add(Utilities.fillInByteArray(userName, 32));
		}
		
		
		try {
			byte[] dataToBeSent = Utilities.combineListOfByteArrays(byteArrays);
			InetAddress destIPAddress = InetAddress.getByName(pair.split(" ")[0]);
			int destPort = Integer.parseInt(pair.split(" ")[1]);
			DatagramPacket packet = 
					new DatagramPacket(dataToBeSent, dataToBeSent.length, 
							destIPAddress, destPort);
			serverSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(cm.getAllUsers().get(pair)+" sent a who request.");

	}
	
	private void handleS2SJoinRequest() {

		// get channel name
		int lastByteOfchannelName;
		for (lastByteOfchannelName = 4; lastByteOfchannelName < 36; lastByteOfchannelName++ ){
			if (receivedRequest[lastByteOfchannelName] == 0) break;
		}
		if (lastByteOfchannelName == 36) lastByteOfchannelName --;
		lastByteOfchannelName --;
		
		byte[] channelName = new byte[lastByteOfchannelName-4+1];
		for (int i = 4; i <= lastByteOfchannelName; i++) {
			channelName[i-4] = receivedRequest[i]; 
		}
		
		String cName = new String(channelName);
		
		//check if the requested join channel exists
		if(!cm.getChannelTable().containsKey(cName)) {
			cm.createChannel(cName); //create
			cm.initializeRoutingTableInChannel(cName, neighbors);
			
			// remove the sender from the routing table 
			 AddressPortPair sender = null;
			try {
				sender = new AddressPortPair(InetAddress.getByName(pair.split(" ")[0]), Integer.parseInt(pair.split(" ")[1]));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			if (sender != null) cm.getChannelTable().get(cName).getRoutingTable().remove(sender);
		}
		
    	
		// get the sender
		AddressPortPair sender = null;
		try {
			sender = new AddressPortPair(InetAddress.getByName(pair.split(" ")[0]), Integer.parseInt(pair.split(" ")[1]));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		
		// send S2S join message to all servers (except the one this server gets message from) in the channel
		byte[] request = S2SRequestGenerator.generateS2SJoinMessage(cName);
		ArrayList<AddressPortPair> receivingServers = cm.getChannelTable().get(cName).getRoutingTable();
		for (AddressPortPair receivingServer: receivingServers) {
			if (!receivingServer.equals(sender)) { // we do not send the S2S join message back to the sender
				try {
			    	// send S2S join message
			    	DatagramPacket packet = 
							new DatagramPacket(request, request.length, 
									receivingServer.getAddress(), receivingServer.getPort());
			   		serverSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// print send prompt
				StringBuilder prompt = new StringBuilder();
				prompt.append(serverSocket.getLocalSocketAddress()).append(" ");
				String receiverAddress = receivingServer.getAddress().toString()
						.replace("localhost", "");
				prompt.append(receiverAddress).append(":").append(receivingServer.getPort()).append(" ");
				prompt.append("send S2S Join ").append(cName);
				System.out.println(prompt);
			}
	    	
		}
		
		// print receive prompt
		StringBuilder prompt = new StringBuilder();
		prompt.append(serverSocket.getLocalSocketAddress()).append(" /");
		prompt.append(pair.split(" ")[0]).append(":").append(pair.split(" ")[1]).append(" ");
		prompt.append("recv S2S Join ").append(cName);
		System.out.println(prompt);
		
	}
	
	private void handleS2SSayRequest() {
		
		// get text field
		int lastByteOfTextField; 
		for (lastByteOfTextField = 76; lastByteOfTextField < 140; lastByteOfTextField++ ){
			if (receivedRequest[lastByteOfTextField] == 0) break;
		}
		if (lastByteOfTextField == 140) lastByteOfTextField --;
		lastByteOfTextField --;
			
		
		byte[] textField = new byte[lastByteOfTextField-76+1];
		for (int i = 76; i <= lastByteOfTextField; i++) {
			textField[i-76] = receivedRequest[i]; 
		}
		
		// get channel name 
		int lastByteOfChannelName;
		for (lastByteOfChannelName = 44; lastByteOfChannelName < 76; lastByteOfChannelName++ ){
			if (receivedRequest[lastByteOfChannelName] == 0) break;
		}
		if (lastByteOfChannelName == 76) lastByteOfChannelName --;
		lastByteOfChannelName --;
		
		byte[] channelName = new byte[lastByteOfChannelName-44+1];
		for (int i = 44; i <= lastByteOfChannelName; i++) {
			channelName[i-44] = receivedRequest[i]; 
		}
		
		// get username
		int lastByteOfUsername;
		for (lastByteOfUsername = 12; lastByteOfUsername < 44; lastByteOfUsername++ ){
			if (receivedRequest[lastByteOfUsername] == 0) break;
		}
		if (lastByteOfUsername == 44) lastByteOfUsername --;
		lastByteOfUsername --;
		
		byte[] userName = new byte[lastByteOfUsername-12+1];
		for (int i = 12; i <= lastByteOfUsername; i++) {
			userName[i-12] = receivedRequest[i]; 
		}
		
		
		
		
		// TODO look for a way to get 64-bit unique identifier
		
		
		// print receive prompt
		StringBuilder receivePrompt = new StringBuilder();
		receivePrompt.append(serverSocket.getLocalSocketAddress()).append(" /");
		receivePrompt.append(pair.split(" ")[0]).append(":").append(pair.split(" ")[1]).append(" ");
		receivePrompt.append("recv S2S Say ").append(channelName);
		System.out.println(receivePrompt);
		
		
		// get the sender
		AddressPortPair sender = null;
		try {
			sender = new AddressPortPair(InetAddress.getByName(pair.split(" ")[0]), Integer.parseInt(pair.split(" ")[1]));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		/*
		 * There are 2 scenarios in which we need to send a S2S leave message to the server that
		 * sends this S2S say message. The 1st scenario is that the server is a leaf, which is 
		 * represented by leafInTree boolean variable. The 2nd scenario is that the server gets 
		 * a duplicate say message, which is represented by gotDuplicateSay boolean variable.
		 * 
		 * For the 1st scenario, we also need to remove the server from the tree.
		 * For the 2nd scenario, we also need to delete the server that sends this S2S say message
		 * from the channel.
		 */
		
		// check if the server is the leaf of the tree for the channel
		boolean leafInTree = false;
		Channel channel = cm.getChannelTable().get(new String(channelName).trim());
		if (channel == null) {
			leafInTree = true;
		} else {
			leafInTree = channel.getClients().size() == 0 && // the channel does not have any clients
					channel.getRoutingTable().size() == 1 && 
					channel.getRoutingTable().get(0).equals(sender); // the server only knows the sender
		
			// remove itself from the tree, i.e., delete the channel in this server's own channel table.
			if (leafInTree)  cm.getChannelTable().remove(new String(channelName).trim());
		}
		
		// check if the server received a duplicate say
		boolean gotDuplicateSay = false;
		// TODO add one more check: duplicate
		
		
		
		//  delete the server that sends this S2S say message from the channel
		if (gotDuplicateSay && channel != null) {
			boolean senderRemovedSuccessfully = channel.getRoutingTable().remove(sender);
			if (!senderRemovedSuccessfully) System.out.println("Error when removing the sender");
		}
		
		// send S2S leave message if necessary. Otherwise, forward S2S say messages to children
		if (leafInTree || gotDuplicateSay) {
			
			byte[] leaveRequest = S2SRequestGenerator.generateS2SLeaveMessage(new String(channelName).trim());
			
			try {
		    	// send S2S leave message
		    	DatagramPacket packet = 
						new DatagramPacket(leaveRequest, leaveRequest.length, 
								sender.getAddress(), sender.getPort());
		   		serverSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// print send Leave prompt
			StringBuilder sendPrompt = new StringBuilder();
			sendPrompt.append(serverSocket.getLocalSocketAddress()).append(" ");
			String receiverAddress = sender.getAddress().toString()
					.replace("localhost", "");
			sendPrompt.append(receiverAddress).append(":").append(sender.getPort()).append(" ");
			sendPrompt.append("send S2S Leave ").append(channelName);
			System.out.println(sendPrompt);
			
		} else { 
			
			if (channel != null) {
				for (AddressPortPair receivingServer: channel.getRoutingTable()) {
					try {
				    	// send S2S say message
				    	DatagramPacket packet = 
								new DatagramPacket(receivedRequest, receivedRequest.length, 
										receivingServer.getAddress(), receivingServer.getPort());
				   		serverSocket.send(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// print send Say prompt
					StringBuilder sendPrompt = new StringBuilder();
					sendPrompt.append(serverSocket.getLocalSocketAddress()).append(" ");
					String receiverAddress = receivingServer.getAddress().toString()
							.replace("localhost", "");
					sendPrompt.append(receiverAddress).append(":").append(receivingServer.getPort()).append(" ");
					sendPrompt.append("send S2S Say ").append(channelName);
					System.out.println(sendPrompt);
			    	
				}
			}
			
		}
		
	}
	
	private void handleS2SLeaveRequest() {
		
	}

	
	private void sendErrorMessage (String errorMsg){
		byte[] errorIdentifier = new byte[4];
		errorIdentifier[0] = 3;
		
		byte[] errorMessage = Utilities.fillInByteArray(errorMsg, 64);
		
		ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();
		byteArrays.add(errorIdentifier);
		byteArrays.add(errorMessage);
		
		try {
			byte[] dataToBeSent = Utilities.combineListOfByteArrays(byteArrays);
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
	
	private void printAllChannelsAndMembers() {
		for (Map.Entry<String, Channel> channelPair: cm.getChannelTable().entrySet()) {
			System.out.println("==========="+channelPair.getKey()+"============");
			for (Map.Entry<String, String> zu : channelPair.getValue().getClients().entrySet()) {
				System.out.println("Address: "+ zu.getKey().split(" ")[0]);
				System.out.println("Port: "+ zu.getKey().split(" ")[1]);
				System.out.println("Name: "+ zu.getValue());
				System.out.println("--------------");
			}
		}
	}
}
