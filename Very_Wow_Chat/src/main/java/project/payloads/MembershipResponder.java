package project.payloads;

import java.util.List;

import project.persistance.entities.Chatroom;
import project.persistance.entities.Membership;
import project.persistance.entities.User;

public class MembershipResponder extends ChatroomResponder {

	private Long lastRead;
	private ChatroomUserRelation userRelation;
	
	/**
	 * initalize a responder though manual input
	 * 
	 * @param chatroomName
	 * @param displayName
	 * @param description
	 * @param listed
	 * @param invited_only
	 * @param lastRead
	 * @param userRelation
	 * @param lastMessageReceived
	 * @param tags
	 */
	public MembershipResponder(String chatroomName, String displayName, String description, Boolean listed,
			Boolean invited_only, Long lastRead, ChatroomUserRelation userRelation, Long lastMessageReceived,
			List<String> tags) {
		super(chatroomName, displayName, description, listed, invited_only, lastMessageReceived, tags);
		this.lastRead = lastRead;
		this.userRelation = userRelation;
	}

	/**
	 * initalize a responder from a membership
	 * 
	 * @param membership
	 */
	public MembershipResponder(Membership membership) {
		super(membership.getChatroom());
		this.lastRead = membership.getLastRead();

		User user = membership.getUser();
		Chatroom chatroom = membership.getChatroom();

		List<Chatroom> o = user.getOwnedChatrooms();
		List<Chatroom> a = user.getAdminOfChatrooms();
		// set the user relation
		if (o.contains(chatroom)) {
			this.userRelation = ChatroomUserRelation.OWNER;
		} else if (a.contains(chatroom)) {
			this.userRelation = ChatroomUserRelation.ADMIN;
		} else {
			this.userRelation = ChatroomUserRelation.MEMBER;
		}
	}

	public Long getLastRead() {
		return lastRead;
	}

	public ChatroomUserRelation getUserRelation() {
		return userRelation;
	}
}
