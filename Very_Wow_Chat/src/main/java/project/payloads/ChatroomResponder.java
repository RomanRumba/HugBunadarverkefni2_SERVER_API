package project.payloads;

import java.util.List;
import java.util.stream.Collectors;

import project.persistance.entities.Chatroom;

/**
 * This class is for wrapping chatrooms in a way that can be replicated in a
 * json object. This is used by controllers to receive json objects to convert
 * to jva class and to convert java classes to json objects when returning
 * resources.
 * 
 * @author Vilhelml
 *
 */
public class ChatroomResponder {
	// unique name serving as an identifier
	private String chatroomName;
	// non-unique name to be displayed
	private String displayName;
	// description of the chatroom
	private String description;
	// denotes the visibility of the chatroom: true means listed, false means
	// unlisted
	private Boolean listed;
	// denots the accessability of the chatroom: true means users can only join with
	// an invite, false means anyone can join
	private Boolean invited_only;
	// the username of the owner of the chatroom
	private String ownerUsername;
	// when the chatroom was created
	private Long created;
	// timestamp of when the latest message was received
	private Long lastMessageReceived;
	// the chatroom's tags
	private List<String> tags;

	/**
	 * Create a responder from a chatroom
	 * 
	 * @param chatroom chatroom to be transformed
	 */
	public ChatroomResponder(Chatroom chatroom) {
		this.chatroomName = chatroom.getChatroomName();
		this.displayName = chatroom.getDisplayName();
		this.description = chatroom.getDescription();
		this.listed = chatroom.getListed();
		this.invited_only = chatroom.getInvited_only();
		this.ownerUsername = chatroom.getOwner() != null ? chatroom.getOwner().getUsername() : "";
		this.created = chatroom.getCreated();
		this.lastMessageReceived = chatroom.getLastMessageReceived();
		this.tags = chatroom.getTags().stream().map(x -> x.getName()).collect(Collectors.toList());
	}

	/**
	 * Constructed used by Spring controller to use JSON objects.
	 * 
	 * @param chatroomName
	 * @param displayName
	 * @param description
	 * @param listed
	 * @param invited_only
	 * @param lastMessageReceived
	 * @param tags
	 */
	public ChatroomResponder(String chatroomName, String displayName, String description, Boolean listed,
			Boolean invited_only, Long lastMessageReceived, List<String> tags) {
		this.chatroomName = chatroomName;
		this.displayName = displayName;
		this.description = description;
		this.listed = listed;
		this.invited_only = invited_only;
		this.lastMessageReceived = lastMessageReceived;
		this.tags = tags;
	}
	// getters

	public String getChatroomName() {
		return chatroomName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getListed() {
		return listed;
	}

	public Boolean getInvited_only() {
		return invited_only;
	}

	public String getOwnerUsername() {
		return ownerUsername;
	}

	public Long getCreated() {
		return created;
	}

	public Long getLastMessageReceived() {
		return lastMessageReceived;
	}

	public void setLastMessageReceived(Long lastMessageReceived) {
		this.lastMessageReceived = lastMessageReceived;
	}

	public List<String> getTags() {
		return tags;
	}

}
