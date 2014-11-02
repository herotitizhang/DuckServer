import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
	
	public static void main (String[] args) {
		
		// setting up
		byte[] receiveData = new byte[1024]; // a placeholder for incoming data
		DatagramSocket serverSocket = null; 
		ChannelManager cm = new ChannelManager();
		ExecutorService threadExecutor = Executors.newCachedThreadPool();
		
		try {
			serverSocket = new DatagramSocket(65533);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		// server starts accepting requests from users and processing them
		while (true) { 
			try {
				// get all the resources needed to respond to a client
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				ClientRequest clientRequest = (ClientRequest)Utilities.getObject(receiveData); // Deserialization occurs
				receiveData = new byte[1024]; // TODO the purpose is to clear the placeholder. is it necessary?
				String pair = receivePacket.getAddress().getHostAddress() +" "+ receivePacket.getPort();
			
				// make a new thread to deliver response to clients so that 
				// the current thread can still receive incoming data
				RequestHandler handleRequestTask = new RequestHandler(cm, serverSocket, clientRequest, pair);
				threadExecutor.execute(handleRequestTask);
			} catch (SocketException e) { //there is an error creating or accessing a Socket.
				e.printStackTrace();
			} catch (IOException e) { //there is an error receiving a DatagramPacket.
				e.printStackTrace();
			} 			
			
		}
			
	}
	
}
