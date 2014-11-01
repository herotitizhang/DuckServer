import java.io.Serializable;
import java.util.HashMap;


public class ServerResponse implements Serializable {
	private int identifier; // 4-byte message type identifier
	private byte[] channelName; // 32-byte channel name
	private byte[] userName; // 32-byte channel name
	private byte[] text; // 64-byte text field
	
	// the following variables are for "list" response
	private int numOfChannels; // 4-byte integer indicating the total number of channels
	private byte[][] channelNames; // an array of channel names
	
	// the following varaibles are for "who" response
	private int numOfUsers; // 4-byte integer indicating the total number of users
	private byte[][] userNames; // an array of usernames
	
	private byte[] errorMessage; // 64-byte error message
	
	// constructor for say response
	public ServerResponse(byte[] channelName, byte[] userName, byte[] text) {
		this.identifier = 0;
		this.channelName = channelName;
		this.userName = userName;
		this.text = text;
	}
	
	// constructor for list response
	public ServerResponse(int numOfChannels, byte[][] channelNames) {
		this.identifier = 1;
		this.numOfChannels = numOfChannels;
		this.channelNames = channelNames;
	}

	// constructor for who response
	public ServerResponse(int numOfUsers, byte[] channelName, byte[][] userNames) {
		this.identifier = 2;
		this.numOfUsers = numOfUsers;
		this.channelName = channelName;
		this.userNames = userNames;
	}

	// constructor for error response
	public ServerResponse(byte[] errorMessage) {
		this.identifier = 3;
		this.errorMessage = errorMessage;
	}
	
	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	public byte[] getChannelName() {
		return channelName;
	}

	public void setChannelName(byte[] channelName) {
		this.channelName = channelName;
	}

	public byte[] getUserName() {
		return userName;
	}

	public void setUserName(byte[] userName) {
		this.userName = userName;
	}

	public byte[] getText() {
		return text;
	}

	public void setText(byte[] text) {
		this.text = text;
	}

	public int getNumOfChannels() {
		return numOfChannels;
	}

	public void setNumOfChannels(int numOfChannels) {
		this.numOfChannels = numOfChannels;
	}

	public byte[][] getChannelNames() {
		return channelNames;
	}

	public void setChannelNames(byte[][] channelNames) {
		this.channelNames = channelNames;
	}

	public int getNumOfUsers() {
		return numOfUsers;
	}

	public void setNumOfUsers(int numOfUsers) {
		this.numOfUsers = numOfUsers;
	}

	public byte[][] getUserNames() {
		return userNames;
	}

	public void setUserNames(byte[][] userNames) {
		this.userNames = userNames;
	}

	public byte[] getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(byte[] errorMessage) {
		this.errorMessage = errorMessage;
	}
}
