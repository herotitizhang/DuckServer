import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
	
	static DatagramSocket serverSocket = null; 
	private static InetAddress serverAddress = null;
	private static int port = 0;
	private static ChannelManager cm = new ChannelManager();
	static ArrayList<AddressPortPair> neighbors= new ArrayList<AddressPortPair>();
	private static ExecutorService threadExecutor = Executors.newCachedThreadPool();
	public static HashMap<String, Integer> joinRecord =null ;
	
	
	public static void main (String[] args) {
		if (args.length%2 !=0){
			System.out.println("Odd number of arguments!");
			System.exit(0);
		}
		
		try {
			serverAddress = InetAddress.getByName(args[0]);
			port = Integer.parseInt(args[1]);
			serverSocket = new DatagramSocket(port,serverAddress);
			
			for (int i = 2; i < args.length; i+=2) {
				InetAddress neighborAddress = InetAddress.getByName(args[i]);
				int neighborPort = Integer.parseInt(args[i+1]);
				neighbors.add(new AddressPortPair(neighborAddress, neighborPort));
			}
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		
		
		

		 Timer timer = new Timer();
	     timer.schedule(new softStateJoin(),0, 60*1000);
	     
	     Timer newTimer = new Timer();
	     newTimer.schedule(new timerCount(),0, 1000);
	     
			for (String key : cm.getChannelTable().keySet()) {
				joinRecord.put(key, 0);
			}
			
			for (String key : joinRecord.keySet()) {
				if(joinRecord.get(key)>120){
					Channel channel = cm.getChannelTable().get(key);
					if (channel != null) {
						
					cm.getChannelTable().get(key).getRoutingTable().remove(new AddressPortPair(serverAddress,port));
					}
				}
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
				RequestHandler handleRequestTask = new RequestHandler(cm, serverSocket, receiveData, pair, neighbors);
				threadExecutor.execute(handleRequestTask);
				
			} catch (SocketException e) { //there is an error creating or accessing a Socket.
				e.printStackTrace();
			} catch (IOException e) { //there is an error receiving a DatagramPacket.
				e.printStackTrace();
			} 			
		}
			
	}
	// timer for ssjoin
	



}
class softStateJoin extends TimerTask {
	
    public void run() {
    
		ChannelManager cm = new ChannelManager();
    	for (String key: cm.getChannelTable().keySet()){
    		
    		byte[] request = S2SRequestGenerator.generateS2SJoinMessage(key);
    		
    		new Server();
			for (AddressPortPair neighbor: Server.neighbors) {
    			
    				try {
			    	// send S2S join message
    					DatagramPacket packet = 
							new DatagramPacket(request, request.length, 
									neighbor.getAddress(), neighbor.getPort());
			   		Server.serverSocket.send(packet);
				} 	catch (IOException e) {
						e.printStackTrace();
					}
    		}
    	}
    	}
}

class timerCount extends TimerTask {
	public void run() {
		for(String key:Server.joinRecord.keySet())
			Server.joinRecord.put(key, Server.joinRecord.get(key)+1);
		
		
	}
	
}

	
