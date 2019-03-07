package project.persistance.entities;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Davíð Helgason (dah38@hi.is)
 */
@Document(collection = "chatMessage")
public class ChatMessage {

	// MongoDB ID
	@Id
	private String id;

	// Rel. to neo4j
	private String chatroomName;

	// Rel. to neo4j
	private long senderUsernameId;
	private String senderUsername;
	private String senderDisplayName;

	// @Convert(converter = MessageConverter.class)
	
	private String message;
	
	/**
	 * SHA512 strings of uploaded resources.
	 * 
	 * null if not in use (I guess...)
	 */
	@Field("resources")
	private List<String> resources;

	@Indexed
	private Long timestamp;
	
	public List<String> getResources() {
		return resources;
	}

	public void setResources(List<String> resources) {
		this.resources = resources;
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

	public String getMessage() {
		return message;    
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		// TODO: add resources string
		return "ChatMessage [id=" + id + ", chatroomName=" + chatroomName + ", senderUsernameId=" + senderUsernameId
				+ ", senderUsername=" + senderUsername + ", senderDisplayName=" + senderDisplayName + ", message="
				+ message + ", timestamp=" + timestamp + "]";
	}

	/*
	public ChatMessage(String id, String chatroomName, long senderUsernameId, String senderUsername,
			String senderDisplayName, String message, Long timestamp) {
		super();
		this.id = id;
		this.chatroomName = chatroomName;
		this.senderUsernameId = senderUsernameId;
		this.senderUsername = senderUsername;
		this.senderDisplayName = senderDisplayName;
		this.message = message;
		this.timestamp = timestamp;
		this.resources = null;
	}*/
	
	public ChatMessage(String id, String chatroomName, long senderUsernameId, String senderUsername,
			String senderDisplayName, String message, Long timestamp, List<String> resources) {
		super();
		this.id = id;
		this.chatroomName = chatroomName;
		this.senderUsernameId = senderUsernameId;
		this.senderUsername = senderUsername;
		this.senderDisplayName = senderDisplayName;
		this.message = message;
		this.timestamp = timestamp;
		this.resources = resources;
	}

}
