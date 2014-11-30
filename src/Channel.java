import java.util.ArrayList;
import java.util.Hashtable;

/**
* The clients hashtable stores <String, String> entries, where the first String
* contains information about a user's IP address and port number, and the 
* second String is the username.
*/

public class Channel {
	private Hashtable<String, String> clients;
	private ArrayList<AddressPortPair> servers;
	
	public Channel() {
		clients = new Hashtable<String, String>();
		servers = new ArrayList<AddressPortPair>();
	}
	
	public Hashtable<String, String> getClients() {
		return clients;
	}
	public void setClients(Hashtable<String, String> clients) {
		this.clients = clients;
	}
	public ArrayList<AddressPortPair> getServers() {
		return servers;
	}
	public void setServers(ArrayList<AddressPortPair> servers) {
		this.servers = servers;
	}	
	
}
