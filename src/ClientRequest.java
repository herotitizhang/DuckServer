import java.io.Serializable;

/**
 * ClientRequest is a data structure that holds 
 * information sent by a client to the server. 
 * 
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class ClientRequest implements Serializable {
	private int identifier = -1; // 4-byte message type identifier
	private byte[] userName; // 32-byte user name
	private byte[] channelName; //32-byte channel name
	private byte[] text; // 64-byte textfield
	
	// constructor for login, join, leave or who request 
	public ClientRequest(int identifier, byte[] name) {
		this(identifier);
		if (identifier == 0) { //login
			userName = name;
		} else if (identifier == 2 || identifier == 3 || identifier == 6){ // join, leave or who
			channelName = name;
		}
	
	}
	// constructor for list or logout request 
	public ClientRequest(int identifier) {
		this.identifier = identifier;
	}
	
	// constructor for say request 
	public ClientRequest(byte[] name, byte[] field) {
		this(4);
		this.channelName = name;
		this.text = field;
	}
	
	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	public byte[] getUserName() {
		return userName;
	}

	public void setUserName(byte[] userName) {
		this.userName = userName;
	}

	public byte[] getChannelName() {
		return channelName;
	}

	public void setChannelName(byte[] channelName) {
		this.channelName = channelName;
	}

	public byte[] getText() {
		return text;
	}

	public void setText(byte[] text) {
		this.text = text;
	}
	
}
