package project.persistance.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import project.persistance.entities.Chatroom;

/**
 * An interface defining methods relating to database functionality
 * of users, such as fetching, creating, updating and deleting.
 * 
 * This interface uses neo4j to store data.
 * 
 * This interface will be implemented by spring behind the scenes.
 * 
 * @author Vilhelml
 */
public interface ChatroomRepository extends Neo4jRepository<Chatroom, Long> {

	/**
	 * Returns chat room with name <code>chatroomName</code>.
	 * 
	 * @param chatroomName Name of chat room.
	 * 
	 * @return Chat room with name <code>chatroomName</code>.
	 */
	Chatroom findByChatroomName(String chatroomName);

	/**
	 * get all listed chat rooms
	 * 
	 * @param listed
	 */
	List<Chatroom> findByListed(Boolean listed);

	/**
	 * get all chat rooms
	 */
	List<Chatroom> findAll();

	/**
	 * Create new chat room in database.
	 * 
	 * @param chatroom
	 */
	Chatroom save(Chatroom chatroom);

	/**
	 * Deletes chat room <code>chatroom</code>.
	 * 
	 * @param chatroom The chat room to delete.
	 */
	void delete(Chatroom chatroom);

	/**
	 * Finds and returns all (public?) chat rooms with tag <code>tagName</code>.
	 * 
	 * @param tagName Tag
	 * 
	 * @return List of chat rooms.
	 */
	@Query("MATCH(a:Chatroom)-[r:HAS_TAG]->(b:Tag) WHERE b.name = {tagName} AND a.listed = true RETURN a.chatroomName;")
	List<String> findListedChatroomsWithTag(@Param("tagName") String tagName);
}
