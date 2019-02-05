package project.payloads;

/**
 * Contains a chatroom name and a timestamp, used for updating when a user last
 * visited the chatroom.
 * 
 * @author Vilhelml
 */
public class ChatStampReceiver {

	// name of the chatroom
	private String chatroomName;
	// timestamp
	private Long timestamp;

	public ChatStampReceiver(String chatroomName, Long timestamp) {
		this.chatroomName = chatroomName;
		this.timestamp = timestamp;
	}

	// getters

	public String getChatroomName() {
		return chatroomName;
	}

	public void setChatroomName(String chatroomName) {
		this.chatroomName = chatroomName;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
