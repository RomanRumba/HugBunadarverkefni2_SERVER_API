package project.controller;

import java.util.Date;
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

import Library.ResponderLister;
import Library.ResponseWrapper;
import project.errors.HttpException;
import project.payloads.ChatroomResponder;
import project.payloads.ErrorResponder;
import project.payloads.MembershipResponder;
import project.payloads.UserResponder;
import project.persistance.entities.Chatroom;
import project.persistance.entities.Membership;
import project.persistance.entities.Tag;
import project.persistance.entities.User;
import project.services.ChatroomService;
import project.services.TagService;
import project.services.UserService;

/**
 * REST controller responsible for chat room management.
 * 
 * @author Vilhelml
 */
@RestController
@RequestMapping("/auth/chatroom")
public class ChatroomController {

	@Autowired
	private ChatroomService chatroomService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TagService tagService;

	/**
	 * @param chatroomName chat room name
	 * 
	 * @return
	 * 
	 * @deprecated temporary method for testing purposes
	 */
	@RequestMapping(path = "/{chatroomName}/updatechatroomlastmessage", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> updateChatroomLastMessage(@PathVariable String chatroomName) {
		try {
			// fetch the chatroom to be updated
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// set the latest message received as now
			chatroom.setLastMessageReceived((new Date()).getTime());
			// save the changes
			chatroomService.saveChatroom(chatroom);
			// prepare the payload
			ChatroomResponder body = new ChatroomResponder(chatroom);
			// return the payload with a status of 200
			return new ResponseEntity<>(body, HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * @param chatroomName chatroomName of the chatroom to be returned
	 * @return if found, return the chatroom with a status code of 200, else error
	 *          message with status code of 404
	 */
	@RequestMapping(path = "/{chatroomName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatroom(@PathVariable String chatroomName) {
		try {
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// wrap the data to send in json format
			ChatroomResponder body = new ChatroomResponder(chatroom);
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * @param chatroomName chatroomName of the chatroom of the membership to be
	 *        returned
	 * @param token 
	 * @return if found, return the membership with a status code of 200, else
	 *          error message with status code of 404
	 */
	@RequestMapping(path = "/{chatroomName}/membership", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getMembership(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// fetch the chatroom
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			Membership membership = this.chatroomService.getUserMembershipOfChatroom(user, chatroom);
			// wrap the data to send in json format
			ChatroomResponder body = new MembershipResponder(membership);
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * @param chatroomName chatroomName of the chatroom to be returned
	 * @param token
	 * @return if chatroom not found: return 404 not found if user is not the
	 *          owner: return 401 unauthorized if successful: return 204 no content
	 */
	@RequestMapping(path = "/{chatroomName}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Object> deleteChatroom(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			if (!chatroomService.isOwner(user, chatroom)) {
				ErrorResponder body = new ErrorResponder();
				body.setError("User is not the chatroom's owner");
				return new ResponseEntity<>(body.getWrappedError(), HttpStatus.UNAUTHORIZED);
			}
			// wrap the data to send in json format
			chatroomService.deleteChatroom(chatroom);
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @param newChatroom, a wrapper for the chatroom data
	 * @param token
	 * 
	 * @return the chatroom that was created, or an error message
	 */
	@RequestMapping(path = "/", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> createChatroom(@RequestBody ChatroomResponder newChatroom,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// create chatroom from the payload
			Chatroom chatroom = new Chatroom(newChatroom.getChatroomName(), newChatroom.getDisplayName(),
					newChatroom.getDescription(), newChatroom.getListed(), newChatroom.getInvited_only());
			// create the chatroom
			chatroomService.createChatroom(user, chatroom);
			// add the tags to the chatroom
			this.tagService.setTags(chatroom, newChatroom.getTags());
			// prepare membership for return
			Membership membership = this.chatroomService.getUserMembershipOfChatroom(user, chatroom);
			// wrap the data to send in json format
			ChatroomResponder body = new MembershipResponder(membership);
			// return the chatroom and a 201 status code
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.CREATED);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * @param newChatroom, a wrapper for the chatroom data
	 * @param token
	 * @param chatroomName
	 * 
	 * @return the chatroom that was created, or an error message
	 */
	@RequestMapping(path = "/{chatroomName}", method = RequestMethod.PATCH, headers = "Accept=application/json")
	public ResponseEntity<Object> updateChatroom(@RequestBody ChatroomResponder newChatroom,
			UsernamePasswordAuthenticationToken token, @PathVariable String chatroomName) {
		try {
			// fetch user from authentication token
			// User user = userService.findByUsername(token.getName());
			// fetch the chatroom to update
			Chatroom chatroom = this.chatroomService.findByChatname(chatroomName);
			// get the new attributes, if provided
			String newDisplayName = newChatroom.getDisplayName() != null ? newChatroom.getDisplayName()
					: chatroom.getDisplayName();
			String newDescription = newChatroom.getDescription() != null ? newChatroom.getDescription()
					: chatroom.getDescription();
			Boolean newListed = newChatroom.getListed() != null ? newChatroom.getListed() : chatroom.getListed();
			Boolean newInvited_only = newChatroom.getInvited_only() != null ? newChatroom.getInvited_only()
					: chatroom.getInvited_only();
			// apply the new attributes
			chatroom.setDisplayName(newDisplayName);
			chatroom.setDescription(newDescription);
			chatroom.setListed(newListed);
			chatroom.setInvited_only(newInvited_only);
			// save the changes to the chatroom
			this.chatroomService.saveChatroom(chatroom);
			// fetch the new tags
			List<String> newTags = newChatroom.getTags();
			// apply the new tags
			this.tagService.setTags(chatroom, newTags);
			// wrap the chatroom data
			ChatroomResponder body = new ChatroomResponder(chatroom);
			// return the chatroom and a 201 status code
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Send an invite from chatroom to user
	 * 
	 * @param chatroomName name of the chatroom to send the request
	 * @param username
	 * @param token
	 * 
	 * @return if found, return the chatroom with a status code of 200, else error
	 *          message with status code of 404
	 */
	@RequestMapping(path = "/{chatroomName}/invite/{username}", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> sendMemberInvitation(@PathVariable String chatroomName, @PathVariable String username,
			UsernamePasswordAuthenticationToken token) {
		try {
			// the user sending the invite (the invitation will be sent by the chatroom,
			// though)
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the invite is for
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// the user receiving the invite
			User invitee = userService.findByUsername(username);
			// if the invitor doesn't have invite privilages
			if (!chatroomService.hasMemberInvitePrivilages(user, chatroom)) {
				ErrorResponder body = new ErrorResponder();
				body.setError("You do not have permission to invite users to this chatroom.");
				return new ResponseEntity<>(body.getWrappedError(), HttpStatus.UNAUTHORIZED);
			}
			// send the invite
			chatroomService.sendMemberInvitation(invitee, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Join an open chatrom or accept n invite
	 * 
	 * @param chatroomName: name of the chatroom to be joined
	 * @param token
	 * 
	 * @return if successful return a status code of 204, else error message with
	 *          status code of 404 for not found, or 401 for unauthorized
	 */
	@RequestMapping(path = "/{chatroomName}/join", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> joinChatroom(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the user wants to join
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// join the room
			chatroomService.joinChatroom(user, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Send an iadmin nvite from chatroom to user
	 * 
	 * @param chatroomName name of the chatroom to send the request
	 * @param username
	 * @param token
	 * 
	 * @return if found, return the chatroom with a status code of 200, else error
	 *          message with status code of 404
	 */
	@RequestMapping(path = "/{chatroomName}/admininvite/{username}", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> sendAdminInvitation(@PathVariable String chatroomName, @PathVariable String username,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the invite is for
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// the user receiving the invite
			User invitee = userService.findByUsername(username);
			// if the invitor doesn't have invite privilages
			if (!chatroomService.hasAdminInvitePrivilages(user, chatroom)) {
				ErrorResponder body = new ErrorResponder();
				body.setError("You do not have permission to invite admins to this chatroom.");
				return new ResponseEntity<>(body.getWrappedError(), HttpStatus.UNAUTHORIZED);
			}
			if (user == invitee) {
				ErrorResponder body = new ErrorResponder();
				body.setError("Cannot send yourself an admin invitation.");
				return new ResponseEntity<>(body.getWrappedError(), HttpStatus.UNAUTHORIZED);
			}
			// send the invite
			chatroomService.sendAdminInvitation(invitee, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Join an open chatrom or accept n invite
	 * 
	 * @param chatroomName name of the chatroom to be joined
	 * @param token
	 * 
	 * @return if successful return a status code of 204, else error message with
	 *          status code of 404 for not found, or 401 for unauthorized
	 */
	@RequestMapping(path = "/{chatroomName}/acceptadmininvite", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> acceptAdminInvite(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the user wants to join
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// accept the invite
			chatroomService.acceptAdminInvite(user, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Join an open chatrom or accept n invite
	 * 
	 * @param chatroomName name of the chatroom to be joined
	 * @param token
	 * 
	 * @return if successful return a status code of 204, else error message with
	 *          status code of 404 for not found, or 401 for unauthorized
	 */
	@RequestMapping(path = "/{chatroomName}/rejectchatroominvite", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Object> rejectMemberInvite(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the user wants to join
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// accept the invite
			chatroomService.rejectChatroomInvitation(user, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Join an open chatrom or accept n invite
	 * 
	 * @param chatroomName: name of the chatroom to be joined
	 * @param token
	 * @return if successful return a status code of 204, else error message with
	 *          status code of 404 for not found, or 401 for unauthorized
	 */
	@RequestMapping(path = "/{chatroomName}/rejectadmininvite", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Object> rejectAdminInvite(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the user wants to join
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// accept the invite
			chatroomService.rejectAdminInvitation(user, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}
	
	/**
	 * Leave a chatroom, this includes losing membership and admin status
	 * 
	 * @param chatroomName
	 * @param token
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/leave", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Object> leaveChatroom(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the user wants to join
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// leave the chatroom
			chatroomService.leaveChatroom(user, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}
	
	/**
	 * Leave a chatroom, this includes losing membership and admin status
	 * 
	 * @param chatroomName
	 * @param token
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/quitadmin", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Object> quitAdmin(@PathVariable String chatroomName,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// the chatroom that the user wants to join
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// leave the chatroom
			chatroomService.quitAdmin(user, chatroom);
			// return successful, no content
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(path = "/listedchatrooms", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getListedChatrooms() {

		List<Chatroom> chatrooms = chatroomService.getAllListedChatrooms();

		// create a list of ChatroomResponders for json return
		List<ChatroomResponder> body = chatrooms.stream().map(x -> new ChatroomResponder(x))
				.collect(Collectors.toList());

		return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);

	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(path = "/chatrooms", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getAllChatrooms() {
		List<Chatroom> chatrooms = chatroomService.getAllChatrooms();

		// create a list of ChatroomResponders for json return
		List<ChatroomResponder> body = chatrooms.stream().map(x -> new ChatroomResponder(x))
				.collect(Collectors.toList());

		return new ResponseEntity<>(body, HttpStatus.OK);
	}

	/**
	 * 
	 * @param token
	 * @param chatroomName
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/markread", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> markChatroomRead(UsernamePasswordAuthenticationToken token,
			@PathVariable String chatroomName) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// fetch the chatroom the user wants to mark as read
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			// get the membership to update
			Membership membership = chatroomService.getUserMembershipOfChatroom(user, chatroom);
			// update the lastRead timestamp
			membership.setLastRead((new Date()).getTime());
			// save the changes
			userService.saveUser(user);
			// prepare the payload
			MembershipResponder body = new MembershipResponder(membership);
			// return the payload with a status of 200
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * 
	 * @param chatroomName
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/tags", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getChatroomTags(@PathVariable String chatroomName) {
		try {
			// fetch the chatroom
			Chatroom chatroom = this.chatroomService.findByChatname(chatroomName);
			// get the tags
			List<Tag> tags = chatroom.getTags();
			// collect the name of the tags
			List<String> body = tags.stream().map(x -> x.getName()).collect(Collectors.toList());
			// return the data with a 200 status
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @param tagName
	 * @return
	 */
	@RequestMapping(path = "/tag/{tagName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getListedChatroomsWithTag(@PathVariable String tagName) {
		// fetch the chatroom
		List<Chatroom> chatrooms = this.tagService.findListedChatroomsWithTag(tagName);

		// create a list of ChatroomResponders for json return
		List<ChatroomResponder> body = ResponderLister.toChatroomResponderList(chatrooms);

		return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
	}

	/**
	 * Set tags.
	 * 
	 * @param chatroomName
	 * @param tagNames
	 * @param token
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/tags", method = RequestMethod.PATCH, headers = "Accept=application/json")
	public ResponseEntity<Object> setChatroomTags(@PathVariable String chatroomName, @RequestBody List<String> tagNames,
			UsernamePasswordAuthenticationToken token) {
		try {
			// fetch user from authentication token
			User user = userService.findByUsername(token.getName());
			// fetch the chatroom
			Chatroom chatroom = this.chatroomService.findByChatname(chatroomName);
			// check if the user has the privileges to perform this action

			if (!this.chatroomService.hasChatroomTagPrivilages(chatroom, user)) {
				ErrorResponder body = new ErrorResponder();
				body.setError("You do not have the privileges to edit this chatroom's tags.");
				return new ResponseEntity<>(body.getWrappedError(), HttpStatus.UNAUTHORIZED);
			}

			// overwrite the tags
			this.tagService.setTags(chatroom, tagNames);
			// return the updates chatroom
			ChatroomResponder body = new ChatroomResponder(chatroom);
			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @param chatroomName
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/members", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getMembers(@PathVariable String chatroomName) {
		try {
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			List<User> users = chatroom.getMembers();

			// create a list of ChatroomResponders for json return
			List<UserResponder> body = users.stream().map(x -> new UserResponder(x)).collect(Collectors.toList());

			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @param chatroomName
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/memberinvitees", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getMemberInvitees(@PathVariable String chatroomName) {
		try {
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			List<User> users = chatroom.getMemberInvitees();

			// create a list of ChatroomResponders for json return
			List<UserResponder> body = users.stream().map(x -> new UserResponder(x)).collect(Collectors.toList());

			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @param chatroomName
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/administrators", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getAdministrators(@PathVariable String chatroomName) {
		try {
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			List<User> users = chatroom.getAdministrators();

			// create a list of ChatroomResponders for json return
			List<UserResponder> body = users.stream().map(x -> new UserResponder(x)).collect(Collectors.toList());

			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * 
	 * @param chatroomName
	 * @return
	 */
	@RequestMapping(path = "/{chatroomName}/admininvitees", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Object> getAdminInvitees(@PathVariable String chatroomName) {
		try {
			Chatroom chatroom = chatroomService.findByChatname(chatroomName);
			List<User> users = chatroom.getAdminInvitees();

			// create a list of ChatroomResponders for json return
			List<UserResponder> body = users.stream().map(x -> new UserResponder(x)).collect(Collectors.toList());

			return new ResponseEntity<>(ResponseWrapper.wrap(body), HttpStatus.OK);
		} catch (HttpException e) {
			return e.getErrorResponseEntity();
		}
	}
}
