package project.payloads;

import project.persistance.entities.ChatMessage;
import project.services.CryptographyService;

/**
 * Messages that are sent from the server to the client are encapsulated by this
 * class.
 * 
 * NOTE: messages that are inserted into this container are assumed to be
 * encrypted, and when they are retrieved from this container they are assumed
 * to be unencrypted.
 */
public class MessageResponse {

	// MongoDB ID
	private String id;

	// Rel. to neo4j
	private String chatroomName;

	// Rel. to neo4j
	private long senderUsernameId;
	private String senderUsername;
	private String senderDisplayName;

	private String message;

	private long timestamp;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChatroomName() {
		return chatroomName;
	}

	public void setChatroomName(String chatroomName) {
		this.chatroomName = chatroomName;
	}

	public long getSenderUsernameId() {
		return senderUsernameId;
	}

	public void setSenderUsernameId(long senderUsernameId) {
		this.senderUsernameId = senderUsernameId;
	}

	public String getSenderUsername() {
		return senderUsername;
	}

	public void setSenderUsername(String senderUsername) {
		this.senderUsername = senderUsername;
	}

	public String getSenderDisplayName() {
		return senderDisplayName;
	}

	public void setSenderDisplayName(String senderDisplayName) {
		this.senderDisplayName = senderDisplayName;
	}

	/**
	 * Returns the message decrypted.
	 * 
	 * @return Decrypted message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Decrypts the encrypted message and stores it.
	 * 
	 * @param message Encrypted message.
	 */
	public void setMessage(String message) {
		// NOTE: Decrypt message
		this.message = CryptographyService.getPlaintext(message);
	}

	@Override
	public String toString() {
		return "MessageResponse [id=" + id + ", chatroomName=" + chatroomName + ", senderUsernameId=" + senderUsernameId
				+ ", senderUsername=" + senderUsername + ", senderDisplayName=" + senderDisplayName + ", message="
				+ message + "]";
	}

	public MessageResponse() {
		this(null, null, 0, null, null, null, 0);
	}

	public MessageResponse(String message) {
		this(null, null, 0, null, null, message, 0);
	}

	/**
	 * Decrypts the encrypted message.
	 * 
	 * @param id
	 * @param chatroomName
	 * @param senderUsernameId
	 * @param senderUsername
	 * @param senderDisplayName
	 * @param message           Encrypted message.
	 * @param timestamp
	 */
	public MessageResponse(String id, String chatroomName, long senderUsernameId, String senderUsername,
			String senderDisplayName, String message, long timestamp) {
		super();
		this.id = id;
		this.chatroomName = chatroomName;
		this.senderUsernameId = senderUsernameId;
		this.senderUsername = senderUsername;
		this.senderDisplayName = senderDisplayName;
		// NOTE: decrypt message here.
		this.message = CryptographyService.getPlaintext(message);
		this.timestamp = timestamp;
	}

	public MessageResponse(ChatMessage chatMessage) {
		this(chatMessage.getId(), chatMessage.getChatroomName(), chatMessage.getSenderUsernameId(),
				chatMessage.getSenderUsername(), chatMessage.getSenderDisplayName(), chatMessage.getMessage(),
				chatMessage.getTimestamp());
	}
}
