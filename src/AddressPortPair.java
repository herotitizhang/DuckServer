import java.net.InetAddress;


public class AddressPortPair {
	private InetAddress address;
	private int port;
	
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
	
	@Override
	public boolean equals(Object another) {
		if (another == null) return false;
		
		if (another instanceof AddressPortPair) {
			
			if (((AddressPortPair)another).getAddress().equals(this.address) && 
					((AddressPortPair)another).getPort() == this.port) {
				return true;
			}
			return false;
		}
		return false;
	}

}
