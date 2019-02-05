package project.persistance.entities;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Date;

import org.neo4j.ogm.annotation.EndNode;

/**
 * Relationship entity for the last time a user read a message in a chatroom
 * 
 * @author Vilhelml
 */
@RelationshipEntity(type = "MEMBER_OF")
public class Membership {

	@Id
	@GeneratedValue
	private Long id;

	@StartNode
	private User user;

	@EndNode
	private Chatroom chatroom;

	// timestamp for the last time the user read a message in the chatroom
	private Long lastRead;
	// timestamp for when the user joined the chatroom as a member
	private Long whenJoined;

	// empty constructor for neo4j
	public Membership() {
	}

	/**
	 * a relation which denotes the last time the user viewed the chatroom
	 * 
	 * @param user
	 * @param chatroom
	 */
	public Membership(User user, Chatroom chatroom) {
		this.user = user;
		this.chatroom = chatroom;

		Long now = (new Date()).getTime(); // current time

		this.lastRead = now;
		this.whenJoined = now;
	}

	// getters and setters

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Chatroom getChatroom() {
		return chatroom;
	}

	public void setLastRead(Long lastRead) {
		this.lastRead = lastRead;
	}

	public Long getLastRead() {
		return lastRead;
	}

	public Long getWhenJoined() {
		return whenJoined;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setChatroom(Chatroom chatroom) {
		this.chatroom = chatroom;
	}

	public void setLastRead(long lastRead) {
		this.lastRead = lastRead;
	}

	public void setWhenJoined(long whenJoined) {
		this.whenJoined = whenJoined;
	}
}
