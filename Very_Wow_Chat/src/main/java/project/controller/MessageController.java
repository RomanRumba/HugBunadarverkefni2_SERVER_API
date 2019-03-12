package project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import Library.ResponseWrapper;
import project.errors.NotFoundException;
import project.payloads.MessageRequest;
import project.payloads.MessageResponse;
import project.persistance.entities.ChatMessage;
import project.persistance.entities.Chatroom;
import project.persistance.entities.User;
import project.services.ChatroomService;
import project.services.ContentAddressableStorageService;
import project.services.CryptographyService;
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

	@Autowired
	protected ContentAddressableStorageService cass;
	
	@Value("${content.directory}")
	private String fileDirectory;

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

	@RequestMapping(path = "/{chatroomName}/message/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatMessage(@PathVariable String chatroomName, @PathVariable String id,
			UsernamePasswordAuthenticationToken token) {
		try {
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);

			if (!chatroomService.isMember(user, chatroom)) {
				return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
						HttpStatus.UNAUTHORIZED);
			}

			// TODO: implement get chat message by id

			return new ResponseEntity<>(ResponseWrapper.wrap(0), HttpStatus.OK);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////


	@RequestMapping(path = "/{chatroomName}/message/{id}/{res}", method = RequestMethod.GET, headers = "Accept=*/*")
	public void getResourceByHashAndId(@PathVariable String chatroomName, @PathVariable String id,
			@PathVariable String res, UsernamePasswordAuthenticationToken token, HttpServletResponse httpServletResponse) {
		
		String hash = res;
		
		System.out.println("id: " + id);
		System.out.println("hash: " + hash);
		
		
		
		try {
			// Find user name based off of JWT token.
			User user = userService.findByUsername(token.getName());
			
			// Get chat room from chat room name.
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			
			if (!chatroomService.isMember(user, chatroom)) {
				httpServletResponse.sendError(404, "Unauthorized");
				httpServletResponse.flushBuffer();
				System.err.println("User isn't member of chatroom");
				return;
			}
			
			ChatMessage chatMessage = messageService.getChatMessage(chatroom, id);
			
			List<String> resources = chatMessage.getResources();
			
			
			if (!resources.contains(hash)) {
				httpServletResponse.sendError(404, "Unauthorized");
				httpServletResponse.flushBuffer();
				System.err.println("Hash doesn't exist");
				return;
			}
			
			String path = Paths.get(fileDirectory, hash).toString();
			
			
			
			File file = new File(path);
			
			InputStream is = new FileInputStream(file);
			
			// TODO: check if file exists
			
			
		    
		    // TODO: create a separate database of content type (something like that...)
		    if (true) {
		    	Tika tika = new Tika();
			    String mimeType = tika.detect(file);
				
				//String mimeType = Files.probeContentType(file.toPath());
				httpServletResponse.addHeader("Content-Type", mimeType);
				System.out.println("MimeType: " + mimeType);
		    }
			
			
			
			ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
			
			boolean success = false;
			
			
			if (file.exists()) {
				
				
				
				
			    // copy it to response's OutputStream
				IOUtils.copy(is, servletOutputStream);
				success = true;
			}
			
			
			
			
			if (!success) {
				httpServletResponse.sendError(404, "Resource not found");
			}
			httpServletResponse.flushBuffer();
			
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		

		
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////

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
	@RequestMapping(path = "/{chatroomName}/messageold", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> addChatMessage(@PathVariable String chatroomName,
			@RequestBody MessageRequest chatMessageRequest, UsernamePasswordAuthenticationToken token) {
		try {
			// Find user name based off of JWT token.
			User user = userService.findByUsername(token.getName());
			// Get chat room from chat room name.
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// Check if user is member of chat room.
			if (chatroomService.isMember(user, chatroom)) {
				// Get current time in milliseconds.
				long timestamp = System.currentTimeMillis();
				// Get POSTed message.
				String chatroomMessage = chatMessageRequest.getMessage();

				// Create chat message to store in data base.
				ChatMessage chatMessage = new ChatMessage(null, chatroomName, user.getId(), user.getUsername(),
						user.getDisplayName(), chatroomMessage, timestamp, null);
				// Add chat message to database.
				messageService.addChatMessage(chatMessage);
				// Update ...
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
	public ResponseEntity<Object> addChatMessage2(@PathVariable String chatroomName,
			@RequestBody String chatMessageJsonString, UsernamePasswordAuthenticationToken token) {
		try {
			// TODO: fix HttpStatus
			
			// TODO: Always return "correct" content-type.
			
			// TODO: when uploading, figure out content-type (optional9:

			JsonArray badMessage = new JsonArray();

			// Find user name based off of JWT token.
			User user = userService.findByUsername(token.getName());

			// Get chat room from chat room name.
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);

			if (!chatroomService.isMember(user, chatroom)) {
				badMessage.add("You don't have access to this chat room.");
				return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()), HttpStatus.UNAUTHORIZED);
			}

			// Check if user is member of chat room.

			Gson gson = new Gson();
			// TODO: this might fail... I don't care.
			JsonObject chatMessage = gson.fromJson(chatMessageJsonString, JsonObject.class);

			if (!chatMessage.has("message")) {
				badMessage.add("JSON missing \"message\" property.");
				return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()), HttpStatus.BAD_REQUEST);
			}

			JsonElement mje = chatMessage.get("message");

			if (!mje.isJsonPrimitive()) {
				badMessage.add("JSON \"message\" must be a string");
				return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()), HttpStatus.BAD_REQUEST);
			}

			String message = mje.getAsString();
			List<String> resourcesAL = new ArrayList<>();

			// Check if JSON request had attachments.
			if (chatMessage.has("attachments")) {

				JsonElement attachmentsje = chatMessage.get("attachments");

				if (!attachmentsje.isJsonArray()) {
					badMessage.add("Attachments must be an array");
					return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()),
							HttpStatus.BAD_REQUEST);
				}

				JsonArray attachmentsja = attachmentsje.getAsJsonArray();

				// TODO: I'd ideally want to check whether attachments are correctly formatted
				// before saving

				for (JsonElement attachmentje : attachmentsja) {

					if (!attachmentje.isJsonObject()) {
						badMessage.add("Each attachment must be a JSON object.");
						return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()),
								HttpStatus.BAD_REQUEST);
					}

					JsonObject attachmentjo = attachmentje.getAsJsonObject();

					if (!attachmentjo.has("type")) {
						badMessage.add("Each attachment specificy what type it is, e.g. base64file");
						return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()),
								HttpStatus.BAD_REQUEST);
					}

					String type = attachmentjo.get("type").getAsString();

					if (type.equals("base64file")) {

						if (!attachmentjo.has("value")) {
							badMessage.add("base64file attachment MUST have a value property");
							return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()),
									HttpStatus.BAD_REQUEST);
						}

						String value = attachmentjo.get("value").getAsString();

						byte[] valueBytes = DatatypeConverter.parseBase64Binary(value);

						String hex = cass.storeBytes(valueBytes);
						resourcesAL.add(hex);

					} else {
						System.out.println("Unknown type: " + type);
						badMessage.add("Unknown type for attachment: " + type);
						return new ResponseEntity<>(ResponseWrapper.badWrap(badMessage.toString()),
								HttpStatus.BAD_REQUEST);
					}
				}
			}

			long timestamp = System.currentTimeMillis();

			String[] resources = resourcesAL.toArray(new String[resourcesAL.size()]);

			if (resources.length == 0) {
				resources = null;
			}

			ChatMessage cm = new ChatMessage(null, chatroomName, user.getId(), user.getUsername(),
					user.getDisplayName(), CryptographyService.getCiphertext(message), timestamp, resourcesAL);
			messageService.addChatMessage(cm);
			chatroomService.updateLastMessageReceived(chatroomName);

			return new ResponseEntity<>(ResponseWrapper.wrap(timestamp), HttpStatus.OK);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Sends a message to chat room `chatroomName`.
	 * 
	 * The format of the JSON message should be like,
	 * 
	 * <pre>
	 * { "message": "Hello world!" }
	 * </pre>
	 * 
	 * or
	 * 
	 * <pre>
	 * { 
	 *   "message": "My images!",
	 *   "attachments": [
	 *     "doge.jpeg",
	 *     "woof.txt"
	 *   ]
	 * }
	 * </pre>
	 * 
	 * If you decide to send attachments you'll get a response of links telling you
	 * where to upload your files and where to finalize the sending of the message.
	 * 
	 * If you do not send files then the response you get is what other users are
	 * expected to see.
	 * 
	 * @param chatroomName       Name of chat room.
	 * @param chatMessageRequest The message that is being sent.
	 * @param token              User name and password authentication token.
	 * @deprecated
	 * 
	 * @return Posts a chat message.
	 */
	@RequestMapping(path = "/{chatroomName}/msg", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> addChatMsg(@PathVariable String chatroomName,
			@RequestBody String chatMessageJsonString, UsernamePasswordAuthenticationToken token) {
		try {
			// Find user name based off of JWT token.
			User user = userService.findByUsername(token.getName());

			// Get chat room from chat room name.
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);

			// Check if user is member of chat room.
			if (chatroomService.isMember(user, chatroom)) {
				// Get current time in milliseconds.

				Gson gson = new Gson();
				// TODO: this might fail... I don't care.
				JsonObject chatMessage = gson.fromJson(chatMessageJsonString, JsonObject.class);

				if (chatMessage.has("message")) {

					JsonElement jsonMessage = chatMessage.get("message");

					// TODO: maybe make some checks...
					String message = jsonMessage.getAsString();

					// Check if it has attachments

					if (chatMessage.has("attachments")) {
						// This is where things become more complex!

						JsonElement attachmentsEl = chatMessage.get("attachments");

						if (attachmentsEl.isJsonArray()) {
							JsonArray attachmentsAr = attachmentsEl.getAsJsonArray();

							final int size = attachmentsAr.size();

							String[] attachments = new String[size];

							for (int i = 0; i < size; i += 1) {
								JsonElement pathEl = attachmentsAr.get(i);
								// TODO: ensure element is string!! IF NOT STRING
								// THEN FAIL!
								String path = pathEl.getAsString();
								attachments[i] = path;
							}

							// TODO do something more complex!

						}
					} else {
						// "Simple" message.

						// TODO: implement posting message to database and respond
						// with something sensible.

						return new ResponseEntity<>(ResponseWrapper.wrap("ok"), HttpStatus.OK);
					}
				}
			} else {
				// Add something negative to response!
			}

			// If this point is reached then essentially that means failure.
			// TODO: this response is not good enough!
			return new ResponseEntity<>(ResponseWrapper.badWrap("You don't have access to this chat room."),
					HttpStatus.UNAUTHORIZED);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return e.getErrorResponseEntity();
		}
	}

}
