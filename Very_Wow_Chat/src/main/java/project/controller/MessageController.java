package project.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Library.ResponseWrapper;
import project.errors.NotFoundException;
import project.payloads.MessageRequest;
import project.payloads.MessageResponse;
import project.persistance.entities.ChatMessage;
import project.persistance.entities.Chatroom;
import project.persistance.entities.User;
import project.services.ChatroomService;
import project.services.MessageService;
import project.services.UserService;

/**
 * Message controller. A REST controller for posting and retrieving messages.
 * 
 * NOTE: it's always OK to use this methods even though the user is not a member
 * of a chat room.
 * 
 * @author Davíð Helgason (dah38@hi.is)
 */
@RestController
@RequestMapping("/auth/chatroom")
public class MessageController {

	@Autowired
	protected MessageService messageService;

	@Autowired
	protected ChatroomService chatroomService;

	@Autowired
	protected UserService userService;

	/**
	 * Returns all messages of chat room `chatroomName`.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param token        User name and password authentication token.
	 * 
	 * @return List of chat messages.
	 */
	@RequestMapping(path = "/{chatroomName}/messages/all", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatlogPage(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				List<ChatMessage> messages = messageService.getAllMessages(chatroom);
				List<MessageResponse> body = messages.stream().map(x -> new MessageResponse(x))
						.collect(Collectors.toList());
				return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						ResponseWrapper.badWrap("Offset and limit have to be non-negative integers."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Returns `limit` messages from chat room `chatroomName` starting from
	 * `offset`.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param offset       Where to start retrieving messages.
	 * @param token        User name and password authentication token.
	 * 
	 * @return List of chat messages.
	 */
	@RequestMapping(path = "/{chatroomName}/messages/{offset}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatlogPage(@PathVariable String chatroomName, @PathVariable int offset,
			UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				if (offset >= 0) {
					List<ChatMessage> messages = messageService.getChatPage(chatroom, offset);
					List<MessageResponse> body = messages.stream().map(x -> new MessageResponse(x))
							.collect(Collectors.toList());
					return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							ResponseWrapper.badWrap("Offset and limit have to be non-negative integers."),
							HttpStatus.UNAUTHORIZED);
				}
			} else {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			// e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Returns `limit` messages from chat room `chatroomName` starting from
	 * `offset`.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param limit        How many messages at most to retrieve.
	 * @param offset       Where to start retrieving messages.
	 * @param token        User name and password authentication token.
	 * 
	 * @return List of chat messages.
	 */
	@RequestMapping(path = "/{chatroomName}/messages/{offset}/{limit}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatlogPage(@PathVariable String chatroomName, @PathVariable int limit,
			@PathVariable int offset, UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				if (limit >= 0 && offset >= 0) {
					List<ChatMessage> messages = messageService.getChatPage(chatroom, offset, limit);
					List<MessageResponse> body = messages.stream().map(x -> new MessageResponse(x))
							.collect(Collectors.toList());
					return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							ResponseWrapper.badWrap("Offset and limit have to be non-negative integers."),
							HttpStatus.UNAUTHORIZED);
				}
			} else {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Returns all message from chat room `chatroomName` from time `startTime` until
	 * now.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param startTime    Start Unix time in milliseconds.
	 * @param token        User name and password authentication token.
	 * 
	 * @return List of chat messages.
	 */
	@RequestMapping(path = "/{chatroomName}/messages/time/{startTime}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatroomMessagesFromStartTime(@PathVariable String chatroomName,
			@PathVariable Long startTime, UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				List<ChatMessage> messages = messageService.getChatroomMessagesBetweenTime(chatroom, startTime,
						System.currentTimeMillis());
				List<MessageResponse> body = messages.stream().map(x -> new MessageResponse(x))
						.collect(Collectors.toList());
				return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Returns all messages from chat room `chatroomName` starting from time
	 * `startTime` until time `endTime`.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param startTime    Start Unix time in milliseconds.
	 * @param endTime      End Unix time in milliseconds.
	 * @param token        User name and password authentication token.
	 * 
	 * @return List of chat messages.
	 */
	@RequestMapping(path = "/{chatroomName}/messages/time/{startTime}/{endTime}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatroomMessagesBetweenTime(@PathVariable String chatroomName,
			@PathVariable Long startTime, @PathVariable Long endTime, UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				List<ChatMessage> messages = messageService.getChatroomMessagesBetweenTime(chatroom, startTime,
						endTime);
				List<MessageResponse> body = messages.stream().map(x -> new MessageResponse(x))
						.collect(Collectors.toList());
				return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Returns the number of messages in chat room `chatroomName`.
	 * 
	 * @param chatroomName Name of chat room.
	 * @param token        User name and password authentication token.
	 * 
	 * @return List of chat messages.
	 */
	@RequestMapping(path = "/{chatroomName}/messages/count", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatroomMessagesBetweenTime(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				long count = messageService.getNrOfMessage(chatroom);
				return new ResponseEntity<>(ResponseWrapper.wrap(count), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Sends a message at chat room `chatroomName`.
	 * 
	 * The format of the JSON message should be like,
	 * 
	 * <pre>
	 * { "message": "Hello world!" }
	 * </pre>
	 * 
	 * @param chatroomName       Name of chat room.
	 * @param chatMessageRequest The message that is being sent.
	 * @param token              User name and password authentication token.
	 * 
	 * @return Posts a chat message.
	 */
	@RequestMapping(path = "/{chatroomName}/message", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> addChatMessage(@PathVariable String chatroomName,
			@RequestBody MessageRequest chatMessageRequest, UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (chatroomService.isMember(user, chatroom)) {
				long timestamp = System.currentTimeMillis();
				String chatroomMessage = chatMessageRequest.getMessage();
				ChatMessage chatMessage = new ChatMessage(null, chatroomName, user.getId(), user.getUsername(),
						user.getDisplayName(), chatroomMessage, timestamp);
				messageService.addChatMessage(chatMessage);
				chatroomService.updateLastMessageReceived(chatroomName);
				return new ResponseEntity<>(ResponseWrapper.wrap(timestamp), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}
}
