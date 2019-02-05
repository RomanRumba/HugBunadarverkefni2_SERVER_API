package project.persistance.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import project.persistance.entities.ChatMessage;

/**
 * An interface for fetching and posting chat messages.
 */
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageRepositoryCustom {
	// *crickets*
}
