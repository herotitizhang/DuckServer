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
	
	private static DatagramSocket serverSocket = null; 
	private static int port = 65533;
	private static ChannelManager cm = new ChannelManager();
	private static ExecutorService threadExecutor = Executors.newCachedThreadPool();
	private static byte[] receiveData = new byte[1024]; // a placeholder for incoming data
	
	public static void main (String[] args) {
		
		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		// server starts accepting requests from users and 
		// make a new thread to process each request it receives
		while (true) { 
			
			try {
			
				// get all the resources needed to respond to a client
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				ClientRequest clientRequest = (ClientRequest)Utilities.getObject(receiveData); // Deserialization occurs
				String pair = receivePacket.getAddress().getHostAddress() +" "+ receivePacket.getPort();
				receiveData = new byte[1024]; // TODO the purpose is to clear the placeholder. is it necessary?

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
