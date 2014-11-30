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

	ChannelManager cm;
	DatagramSocket serverSocket;
	byte[] clientRequest; 
	String pair;
	ArrayList<AddressPortPair> neighbors;
	
	public RequestHandler(ChannelManager cm, DatagramSocket serverSocket, 
			byte[] clientRequest, String pair, ArrayList<AddressPortPair> neighbors) {
		this.cm = cm;
		this.serverSocket = serverSocket;
		this.clientRequest = clientRequest;
		this.pair = pair;
		this.neighbors = neighbors;
	}
	
	@Override
	public void run() {
		if (clientRequest[0] == 0) { // login request
			handleLoginRequest();
		} else if (clientRequest[0] == 1) { // logout request
			handleLogoutRequest();
		} else if (clientRequest[0] == 2){ // join request
			handleJoinRequest();
		} else if (clientRequest[0] == 3){ // leave request
			handleLeaveRequest();
		} else if (clientRequest[0] == 4) { // say request
			handleSayRequest();
		} else if (clientRequest[0] == 5) { // list request
			handleListRequest();
		} else if (clientRequest[0] == 6) { // who request
			handleWhoRequest();
		}
		
	}

	private void handleLoginRequest() {
		
		printAllChannelsAndMembers();
		
		// get channel name
		int lastByteOfUserName;
		for (lastByteOfUserName = 4; lastByteOfUserName < 36; lastByteOfUserName++) {
			if (clientRequest[lastByteOfUserName] == 0)
				break;
		}
		if (lastByteOfUserName == 36)
			lastByteOfUserName--;
		lastByteOfUserName--;

		byte[] userName = new byte[lastByteOfUserName - 4 + 1];
		for (int i = 4; i <= lastByteOfUserName; i++) {
			userName[i - 4] = clientRequest[i];
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
			if (clientRequest[lastByteOfchannelName] == 0) break;
		}
		if (lastByteOfchannelName == 36) lastByteOfchannelName --;
		lastByteOfchannelName --;
		
		byte[] channelName = new byte[lastByteOfchannelName-4+1];
		for (int i = 4; i <= lastByteOfchannelName; i++) {
			channelName[i-4] = clientRequest[i]; 
		}
		
		String cName = new String(channelName);
		
		//check if the requested join channel exists
		if(!cm.getChannelTable().containsKey(cName)) {
			cm.createChannel(cName); //create
		}
		
		System.out.println("pair = "+pair+", userName = "+userName);
		cm.addUserToChannel(cName, pair, userName);
		
		System.out.println(userName+" joined channel "+cName+".");
		
		
		
	}
	
	private void handleLeaveRequest() {

		// get channel name
		int lastByteOfchannelName;
		for (lastByteOfchannelName = 4; lastByteOfchannelName < 36; lastByteOfchannelName++) {
			if (clientRequest[lastByteOfchannelName] == 0)
				break;
		}
		if (lastByteOfchannelName == 36)
			lastByteOfchannelName--;
		lastByteOfchannelName--;

		byte[] channelName = new byte[lastByteOfchannelName - 4 + 1];
		for (int i = 4; i <= lastByteOfchannelName; i++) {
			channelName[i - 4] = clientRequest[i];
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
			if (clientRequest[lastByteOfChannelName] == 0) break;
		}
		if (lastByteOfChannelName == 36) lastByteOfChannelName --;
		lastByteOfChannelName --;
		
		byte[] channelName = new byte[32];
		for (int i = 4; i <= lastByteOfChannelName; i++) {
			channelName[i-4] = clientRequest[i]; 
		}
		
		// create userName
		String uName = cm.getAllUsers().get(pair);
		byte[] userName = Utilities.fillInByteArray(uName, 32);
		
		// create textField
		int lastByteOfTextField;
		for (lastByteOfTextField = 36; lastByteOfTextField < 100; lastByteOfTextField++ ){
			if (clientRequest[lastByteOfTextField] == 0) break;
		}
		if (lastByteOfTextField == 100) lastByteOfTextField --;
		lastByteOfTextField --;
		
		byte[] textField = new byte[64];
		for (int i = 36; i <= lastByteOfTextField; i++) {
			textField[i-36] = clientRequest[i]; 
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
			if (clientRequest[lastByteOfChannelName] == 0) break;
		}
		if (lastByteOfChannelName == 36) lastByteOfChannelName --;
		lastByteOfChannelName --;
		
		byte[] channelName = new byte[32];
		for (int i = 4; i <= lastByteOfChannelName; i++) {
			channelName[i-4] = clientRequest[i]; 
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
