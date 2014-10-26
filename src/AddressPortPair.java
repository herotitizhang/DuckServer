import java.net.InetAddress;

/**
 * AddressPortPair is a class that contains a user's IP address and port number
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class AddressPortPair {
	
	InetAddress address;
	int port;
	
	public AddressPortPair(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}