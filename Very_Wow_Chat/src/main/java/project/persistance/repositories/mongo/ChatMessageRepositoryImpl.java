package project.persistance.repositories.mongo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import project.persistance.entities.ChatMessage;

/**
 * Implementation of custom methods for message repository.
 */
@Repository
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * Returns up to <code>limit</code> chat messages from chat room with name
	 * <code>chatroomName</code> starting from <code>offset</code>.
	 * 
	 * If M[1..n] was the list of all messages for chat room C, then this method
	 * would return M[offset, offset + limit], or M[offset, n] if offset + limit &gt;
	 * n.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param offset
	 * @param limit        How many messages at most to fetch (if they exist).
	 * 
	 * @return List of chat messages.
	 */
	@Override
	public List<ChatMessage> findPagedResultByChatroomName(String chatroomName, int offset, int limit) {
		Criteria criteria = Criteria.where("chatroomName").is(chatroomName);
		Query query = new Query(criteria);
		query.skip(offset);
		query.limit(limit);
		List<ChatMessage> results = mongoTemplate.find(query, ChatMessage.class);
		return results;
	}

	/**
	 * Returns all chat messages from chat room with name <code>chatroomName</code>
	 * starting from <code>offset</code> to the end.
	 * 
	 * If M[1..n] was the list of all messages for chat room C, then this method
	 * would return M[offset, n].
	 * 
	 * @param chatroomName Name of chat room.
	 * @param offset
	 * 
	 * @return List of chat messages.
	 */
	@Override
	public List<ChatMessage> findPagedResultByChatroomName(String chatroomName, int offset) {
		Criteria criteria = Criteria.where("chatroomName").is(chatroomName);
		Query query = new Query(criteria);
		query.skip(offset);
		List<ChatMessage> results = mongoTemplate.find(query, ChatMessage.class);
		return results;
	}

	/**
	 * Posts chat message <code>message</code>.
	 * 
	 * TODO: this is a repository so the input should be Java primitives.
	 * 
	 * @param message Chat message to post.
	 */
	@Override
	public void postMessage(ChatMessage message) {
		mongoTemplate.insert(message);
	}

	/**
	 * Returns all message of chat room <code>chatroomName</code>.
	 * 
	 * @param chatroomName Name of chat room.
	 * 
	 * @return List of chat messages.
	 */
	@Override
	public List<ChatMessage> getAllMessages(String chatroomName) {
		Criteria criteria = Criteria.where("chatroomName").is(chatroomName);
		Query query = new Query(criteria);
		List<ChatMessage> results = mongoTemplate.find(query, ChatMessage.class);
		return results;
	}

	/**
	 * Deletes all chat messages of chat room <code>chatroomName</code>.
	 * 
	 * @param chatroomName Name of chat room.
	 */
	@Override
	public void deleteAllChatMessagesOfChatroom(String chatroomName) {
		Criteria criteria = Criteria.where("chatroomName").is(chatroomName);
		Query query = new Query(criteria);
		mongoTemplate.findAllAndRemove(query, ChatMessage.class);
	}

	/**
	 * Returns all chat message for chat room <code>chatroomName</code> that were
	 * posted between <code>startTime</code> and <code>endTime</code>.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param startTime    Start Unix time in milliseconds.
	 * @param endTime      End Unix time in milliseconds.
	 * 
	 * @return List of messages.
	 */
	@Override
	public List<ChatMessage> getChatroomMessagesBetweenTime(String chatroomName, long startTime, long endTime) {
		Query query = new Query();
		query.addCriteria(Criteria.where("chatroomName").is(chatroomName));
		query.addCriteria(Criteria.where("timestamp").gte(startTime).lte(endTime));
		List<ChatMessage> results = mongoTemplate.find(query, ChatMessage.class);
		return results;
	}

	/**
	 * Posts chat message <code>message</code>.
	 * 
	 * TODO: this is a repository so the input should be Java primitives.
	 * 
	 * @param message Chat message to post.
	 */
	@Override
	public void addChatMessage(ChatMessage message) {
		mongoTemplate.insert(message);
	}

	/**
	 * Returns the number of messages that exist for chat room
	 * <code>chatroomName</code>.
	 * 
	 * @return List of chat messages.
	 */
	@Override
	public long getNrOfMessage(String chatroomName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("chatroomName").is(chatroomName));
		long count = mongoTemplate.count(query, ChatMessage.class);
		return count;
	}

}
