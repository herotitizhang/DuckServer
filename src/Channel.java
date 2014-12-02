import java.util.ArrayList;
import java.util.Hashtable;

/**
* The clients hashtable stores <String, String> entries, where the first String
* contains information about a user's IP address and port number, and the 
* second String is the username.
*/

public class Channel {
	private Hashtable<String, String> clients;
	
	// this field initially contains all neighbors when the first join message is received.
	// loops are broken and server's address and port may be deleted when the first say 
	// message is received
	private ArrayList<AddressPortPair> routingTable; 
	
	public Channel() {
		clients = new Hashtable<String, String>();
		routingTable = new ArrayList<AddressPortPair>();
	}
	
	public Hashtable<String, String> getClients() {
		return clients;
	}
	
	public void setClients(Hashtable<String, String> clients) {
		this.clients = clients;
	}
	
	public ArrayList<AddressPortPair> getRoutingTable() {
		return routingTable;
	}
	
	public void setRoutingTable(ArrayList<AddressPortPair> servers) {
		this.routingTable = servers;
	}	
	
}
