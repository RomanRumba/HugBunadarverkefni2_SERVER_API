package project.persistance.repositories.mongo;

import java.util.List;

import project.persistance.entities.ChatMessage;

/**
 * Custom chat message methods that are implemented in
 * <code>ChatMessageRepositoryCustom.java</code>.
 */
public interface ChatMessageRepositoryCustom {

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
	List<ChatMessage> findPagedResultByChatroomName(String chatroomName, int offset, int limit);

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
	List<ChatMessage> findPagedResultByChatroomName(String chatroomName, int offset);

	/**
	 * Returns all message of chat room <code>chatroomName</code>.
	 * 
	 * @param chatroomName Name of chat room.
	 * 
	 * @return List of chat messages.
	 */
	List<ChatMessage> getAllMessages(String chatroomName);

	/**
	 * Posts chat message <code>message</code>.
	 * 
	 * TODO: this is a repository so the input should be Java primitives.
	 * 
	 * @param message Chat message to post.
	 */
	void postMessage(ChatMessage message);

	/**
	 * Deletes all chat messages of chat room <code>chatroomName</code>.
	 * 
	 * @param chatroomName Name of chat room.
	 */
	void deleteAllChatMessagesOfChatroom(String chatroomName);

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
	List<ChatMessage> getChatroomMessagesBetweenTime(String chatroomName, long startTime, long endTime);

	/**
	 * Posts chat message <code>message</code>.
	 * 
	 * TODO: this is a repository so the input should be Java primitives.
	 * 
	 * @param message Chat message to post.
	 */
	void addChatMessage(ChatMessage message);

	/**
	 * Returns the number of messages that exist for chat room
	 * <code>chatroomName</code>.
	 * 
	 * @return List of chat messages.
	 */
	long getNrOfMessage(String chatroomName);

}
