package project.persistance.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import project.persistance.entities.Tag;

/**
 * An interface defining methods relating to database functionality of tags,
 * such as fetching, creating, updating and deleting.
 * 
 * This interface uses neo4j to store data.
 * 
 * This interface will be implemented by spring behind the scenes.
 * 
 * @author Vilhelml
 */
public interface TagRepository extends Neo4jRepository<Tag, Long> {

	/**
	 * Return a Tag NodeEntity if tagName exists.
	 * 
	 * @param tagName
	 */
	Tag findByName(@Param("tagName") String tagName);

	/**
	 * Get all chat rooms (not tags?).
	 */
	List<Tag> findAll();

	/**
	 * Create a new Tag in database.
	 * 
	 * @param tag
	 */
	Tag save(Tag tag);

	/**
	 * Delete a Tag.
	 * 
	 * @param tag
	 */
	void delete(Tag tag);

	/**
	 * Delete all nodes that have no relations.
	 */
	@Query("MATCH (n:Tag) WHERE size((n)--())=0 DELETE n;")
	void deleteTagsWithNoRelations();
}
