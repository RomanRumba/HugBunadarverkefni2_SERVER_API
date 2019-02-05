package project.persistance.entities;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Tag for chat rooms.
 * 
 * Chat rooms can be tagged, and users can search for public chat rooms with
 * these chats.
 */
@NodeEntity
public class Tag {

	@Id
	@GeneratedValue
	protected Long id;
	// unique name of the tag
	private String name;

	// list of chatrooms that have a outgoing relation to the tag
	@Relationship(type = "HAS_TAG", direction = Relationship.INCOMING)
	private List<Chatroom> chatroomsWithTag;

	// Empty constructor required as of Neo4j API 2.0.5
	public Tag() {
	}

	/**
	 * Create a new tag
	 * 
	 * @param name
	 */
	public Tag(String name) {
		this.name = name;
	}

	// getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Chatroom> getChatroomsWithTag() {
		if (chatroomsWithTag == null) {
			chatroomsWithTag = new ArrayList<>();
		}
		return chatroomsWithTag;
	}

	public void setChatroomsWithTag(List<Chatroom> chatroomsWithTag) {
		this.chatroomsWithTag = chatroomsWithTag;
	}

}
