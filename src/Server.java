import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;


public class Server {
	
	ChatroomManager cm = new ChatroomManager();
	
	public static void main (String[] args) {
		
		// setting up
		byte[] receiveData = new byte[1024]; // a placeholder for incoming data
		DatagramSocket serverSocket = null; // a socket for both sending and receiving data
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
				processClientRequest(clientRequest);
			} catch (SocketException e) { //there is an error creating or accessing a Socket.
				e.printStackTrace();
			} catch (IOException e) { //there is an error receiving a DatagramPacket.
				e.printStackTrace();
			} 			
			
		}
		
	}
	
	public static void processClientRequest(ClientRequest clientRequest) {
		if (clientRequest.getIdentifier() == 0) { // login request
			
		}
		
		
		
		System.out.println(new String(clientRequest.getUserName())+" just entered."); 
	}
}
