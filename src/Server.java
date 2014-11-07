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
	private static InetAddress serverAddress = null;
	private static int port = 0;
	private static ChannelManager cm = new ChannelManager();
	private static ExecutorService threadExecutor = Executors.newCachedThreadPool();
	
	public static void main (String[] args) {
		if (args.length !=2){
			System.out.println("need 2 args");
			System.exit(0);
		}
			
		
		try {
			serverAddress = InetAddress.getByName(args[0]);
			port = Integer.parseInt(args[1]);
			serverSocket = new DatagramSocket(port,serverAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		
		// server starts accepting requests from users and 
		// make a new thread to process each request it receives
		while (true) { 
			
			try {
				
				byte[] receiveData = new byte[1024]; // a placeholder for incoming data

				// get all the resources needed to respond to a client
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String pair = receivePacket.getAddress().getHostAddress() +" "+ receivePacket.getPort();

				// make a new thread to deliver response to clients so that 
				// the current thread can still receive incoming data
				RequestHandler handleRequestTask = new RequestHandler(cm, serverSocket, receiveData, pair);
				threadExecutor.execute(handleRequestTask);
				
			
			} catch (SocketException e) { //there is an error creating or accessing a Socket.
				e.printStackTrace();
			} catch (IOException e) { //there is an error receiving a DatagramPacket.
				e.printStackTrace();
			} 			
			
		}
			
	}
	
}
