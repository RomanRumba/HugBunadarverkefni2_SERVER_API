package project.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.errors.BadRequestException;
import project.errors.NotFoundException;
import project.errors.UnauthorizedException;
import project.persistance.entities.Chatroom;
import project.persistance.entities.Membership;
import project.persistance.entities.User;
import project.persistance.repositories.ChatroomRepository;
// import project.persistance.repositories.TagRepository;
import project.persistance.repositories.UserRepository;

/**
 * This service handles functionality relating to chatrooms, and the chatrooms'
 * relations with other entities.
 * 
 * @author Vilhelml
 *
 */
@Service
public class ChatroomService {

	@Autowired
	private ChatroomRepository chatroomRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagService tagService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private AuthenticationService authenticationService;

	public void updateLastMessageReceived(String chatroomName) {
		try {
			Chatroom chatroom = findByChatname(chatroomName);
			chatroom.setLastMessageReceived((new Date()).getTime());
			saveChatroom(chatroom);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if a chatroom exists with a given chatroomName
	 * 
	 * @param chatroomName a user's userName
	 * @return true if chatroomName is in use, else false
	 */
	public Boolean chatroomExists(String chatroomName) {
		Chatroom chatroom = this.chatroomRepository.findByChatroomName(chatroomName);
		return chatroom != null;
	}

	/**
	 * returns a chatroom if the chatroomName is in use else, returns and error
	 * message
	 * 
	 * @param chatroomName
	 * @return the chatroom
	 * @throws NotFoundException if chatroomName doesn't belong to any chatroom
	 */
	public Chatroom findByChatname(String chatroomName) throws NotFoundException {
		// throw error if user doesn't exist
		if (!chatroomExists(chatroomName)) {
			throw new NotFoundException("Chatroom not found");
		}

		Chatroom chatroom = this.chatroomRepository.findByChatroomName(chatroomName);

		return chatroom;
	}

	/**
	 * save a chatroom, used to apply updates
	 * 
	 * @param chatroom
	 * @return the newly updates
	 */
	@Transactional(readOnly = false)
	public Chatroom saveChatroom(Chatroom chatroom) {
		return chatroomRepository.save(chatroom);
	}

	/**
	 * check if user can send member invites from this chat
	 * 
	 * @param user
	 * @param chatroom
	 * @return
	 */
	public boolean hasMemberInvitePrivilages(User user, Chatroom chatroom) {
		return isOwner(user, chatroom) || isAdmin(user, chatroom); // only the owner and admins can send member
																	// invitation
	}

	/**
	 * check if the user can change the tags of a chatroom
	 * 
	 * @param chatroom
	 * @param user
	 * @return
	 */
	public Boolean hasChatroomTagPrivilages(Chatroom chatroom, User user) {
		return isOwner(user, chatroom) || isAdmin(user, chatroom); // only the owner and admins can edit tags
	}

	/**
	 * check if user can send admin invites from this chat
	 * 
	 * @param user
	 * @param chatroom
	 * @return
	 */
	public boolean hasAdminInvitePrivilages(User user, Chatroom chatroom) {
		return isOwner(user, chatroom);// only the owner of a chatroom can send admin invitations
	}

	/**
	 * join a chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @throws BadRequestException   if you are already a member of the chatroom
	 * @throws UnauthorizedException if you don't have permission to join
	 */
	@Transactional(readOnly = false)
	public void joinChatroom(User user, Chatroom chatroom) throws BadRequestException, UnauthorizedException {
		// already member
		if (isMember(user, chatroom)) {
			throw new BadRequestException("You are already a member of " + chatroom.getChatroomName());
		}
		// can't join
		if (!canJoin(user, chatroom)) {
			throw new UnauthorizedException("You need an invite to join " + chatroom.getChatroomName());
		}

		// delete the member invite, if there is one
		deleteMemberInvitation(user, chatroom);
		// create relation / send the invite
		createMemberRelation(user, chatroom);

		// save the changes
		chatroomRepository.save(chatroom);
	}

	/**
	 * create a chatroom
	 * 
	 * @param newChatroom
	 * @return the newly created chatroom
	 */
	@Transactional(readOnly = false)
	public Chatroom createChatroom(User user, Chatroom newChatroom) throws BadRequestException {
		// throw error if username is taken
		if (chatroomExists(newChatroom.getChatroomName())) {
			throw new BadRequestException("Chatoom name is already in use.");
		}
		// check if chatroomName is valid
		if (!authenticationService.noSymbolsCheck(newChatroom.getChatroomName())) {
			throw new BadRequestException("Chatoom name contains invalid characters.");
		}
		// create a owner relation
		newChatroom.setOwner(user);

		// add chatroom to user's list of owned chatrooms
		List<Chatroom> ownedRooms = user.getOwnedChatrooms();
		ownedRooms.add(newChatroom);

		// create admin relation
		createAdminRelation(user, newChatroom);

		// create member relation
		createMemberRelation(user, newChatroom);

		// save the chatroom, this will also save the user's new relations
		Chatroom chatroom = chatroomRepository.save(newChatroom);
		return chatroom;
	}

	/**
	 * leave a chatroom, this includes quitting as an administrator
	 * 
	 * @param user
	 * @param chatroom
	 * @throws BadRequestException
	 */
	@Transactional(readOnly = false)
	public void leaveChatroom(User user, Chatroom chatroom) throws BadRequestException {
		if (isOwner(user, chatroom)) {
			throw new BadRequestException(
					"Cannot leave a room you own, transfer ownership to someone else before leaving, or delete the chatroom");
		}
		if (!isMember(user, chatroom)) {
			throw new BadRequestException("Need to be a member of a chatroom to leave it.");
		}
		// delete member relation
		deleteMembership(user, chatroom);
		// delete admin relation
		deleteAdminship(user, chatroom);
	}

	/**
	 * delete the member invitation, and the admin invitation if there is one
	 * 
	 * @param user     the invitee
	 * @param chatroom the chatroom
	 * @throws BadRequestException
	 */
	@Transactional(readOnly = false)
	public void rejectChatroomInvitation(User user, Chatroom chatroom) throws BadRequestException {
		// if there is no invite
		if (!this.memberInvitationSent(user, chatroom)) {
			throw new BadRequestException("There is no invite to decline");
		}
		// delete the relation
		this.deleteMemberInvitation(user, chatroom);
		// delete the admin invite if it is there
		this.deleteAdminInvitation(user, chatroom);
	}

	/**
	 * delete the
	 * 
	 * @param user     the invitee
	 * @param chatroom the chatroom
	 * @throws BadRequestException
	 */
	@Transactional(readOnly = false)
	public void rejectAdminInvitation(User user, Chatroom chatroom) throws BadRequestException {
		// if there is no invite
		if (!this.adminInvitationSent(user, chatroom)) {
			throw new BadRequestException("There is no invite to decline");
		}
		// delete the relation
		this.deleteAdminInvitation(user, chatroom);
	}

	/**
	 * stop being an administrator of a chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @throws BadRequestException
	 */
	@Transactional(readOnly = false)
	public void quitAdmin(User user, Chatroom chatroom) throws BadRequestException {
		if (isOwner(user, chatroom)) {
			throw new BadRequestException(
					"Cannot leave a room you own, transfer ownership to someone else before leaving, or delete the chatroom");
		}
		if (!isAdmin(user, chatroom)) {
			throw new BadRequestException("Need to be an administrator to quit being an administrator.");
		}
		// delete admin relation
		deleteAdminship(user, chatroom);
	}

	/**
	 * check if the user can join a given chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if he can, else false
	 */
	public boolean canJoin(User user, Chatroom chatroom) {
		// invites are needed to join the chatroom and the useer has not received an
		// invite
		if (chatroom.getInvited_only() && !memberInvitationSent(user, chatroom)) {
			return false;
		}
		return true;
	}

	/**
	 * check if the user can become an admin of the chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if he can, else false
	 */
	public boolean canBecomeAdmin(User user, Chatroom chatroom) {
		return adminInvitationSent(user, chatroom);
	}

	/**
	 * send a member invitation from chatroom to username
	 * 
	 * @param user
	 * @param chatroom
	 * @throws BadRequestException if user is already member or already has bending
	 *                             invite
	 */
	public void sendMemberInvitation(User user, Chatroom chatroom) throws BadRequestException {
		if (isMember(user, chatroom)) {
			throw new BadRequestException("User is already a member of this chatroom");
		}
		if (memberInvitationSent(user, chatroom)) {
			throw new BadRequestException("User already has a pending member request");
		}
		// send member invitation
		createMemberInvitation(user, chatroom);
	}

	/**
	 * send an admin invitation to a chatroom to the user
	 * 
	 * @param user
	 * @param chatroom
	 * @throws BadRequestException if user already has a pending admin invite
	 */
	@Transactional(readOnly = false)
	public void sendAdminInvitation(User user, Chatroom chatroom) throws BadRequestException {
		if (adminInvitationSent(user, chatroom)) {
			throw new BadRequestException("The user already has a pending admin invitation");
		}
		if (isAdmin(user, chatroom)) {
			throw new BadRequestException("The user is already an admin of the chatroom");
		}
		if (!this.isMember(user, chatroom)) {
			this.createMemberInvitation(user, chatroom);
		}
		createAdminInvitation(user, chatroom);
	}

	/**
	 * join a chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @throws BadRequestException   if you are already a member of the chatroom
	 * @throws UnauthorizedException if you don't have permission to join
	 */
	@Transactional(readOnly = false)
	public void acceptAdminInvite(User user, Chatroom chatroom) throws BadRequestException, UnauthorizedException {
		// already admin
		if (isAdmin(user, chatroom)) {
			throw new BadRequestException("You are already an admin of this chatroom.");
		}
		// can't join
		if (!canBecomeAdmin(user, chatroom)) {
			throw new UnauthorizedException("You need an invite to become an admin of this chatroom.");
		}

		// if the user has received a member invite aswell, delete it
		deleteMemberInvitation(user, chatroom);
		// delete the member invite, if there is one
		deleteAdminInvitation(user, chatroom);
		// create admin relation
		createAdminRelation(user, chatroom);
		// admins are also members
		if (!isMember(user, chatroom)) {
			createMemberRelation(user, chatroom);
		}
	}

	/**
	 * Get all the chatrooms
	 * 
	 * @return list of all chatrooms
	 */
	public List<Chatroom> getAllChatrooms() {
		// fetch chatrooms from database
		List<Chatroom> chatrooms = chatroomRepository.findAll();

		return chatrooms;
	}

	/**
	 * Get all the listed chatrooms
	 * 
	 * @return list of listed chatrooms
	 */
	public List<Chatroom> getAllListedChatrooms() {
		// fetch chatrooms from database
		List<Chatroom> chatrooms = chatroomRepository.findByListed(true);

		return chatrooms;
	}

	/**
	 * Check if a user is a member of a chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if user is a member, else false
	 */
	public boolean isMember(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getMemberOfChatrooms();
		List<User> users = chatroom.getMembers();

		return chatrooms.contains(chatroom) && users.contains(user);
	}

	/**
	 * Check if a user is an administrator of a chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if user is an administrator, else false
	 */
	public boolean isAdmin(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getAdminOfChatrooms();
		List<User> users = chatroom.getAdministrators();

		return chatrooms.contains(chatroom) && users.contains(user);
	}

	/**
	 * Check if a user is the owner of a chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if user is owner, else false
	 */
	public boolean isOwner(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getOwnedChatrooms();

		return chatrooms.contains(chatroom) && chatroom.getOwner() == user;
	}

	/**
	 * Check if a user has received a membership invite to the chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if a user has received a membership invite to the chatroom, else
	 *         false
	 */
	public boolean memberInvitationSent(User user, Chatroom chatroom) {
		List<Chatroom> chatroomInvites = user.getChatroomInvites();
		List<User> invitees = chatroom.getMemberInvitees();

		return invitees.contains(user) && chatroomInvites.contains(chatroom);
	}

	/**
	 * Check if a user has received an admin invite to the chatroom
	 * 
	 * @param user
	 * @param chatroom
	 * @return true if a user has received an admin invite to the chatroom, else
	 *         false
	 */
	public boolean adminInvitationSent(User user, Chatroom chatroom) {
		List<Chatroom> adminInvites = user.getChatroomAdminInvites();
		List<User> admins = chatroom.getAdminInvitees();

		return admins.contains(user) && adminInvites.contains(chatroom);
	}

	/**
	 * Delete chatroom <code>chatroom</code> and all its relations, also all
	 * associated chat messages.
	 * 
	 * @param chatroom The chat room to delete.
	 */
	@Transactional(readOnly = false)
	public void deleteChatroom(Chatroom chatroom) {
		// delete the chat logs
		messageService.deleteAllChatMessagesOfChatroom(chatroom);
		// remove the tags
		tagService.removeAllTagsFromChatroom(chatroom);
		// delete the chatroom
		chatroomRepository.delete(chatroom);
	}

	public Membership getUserMembershipOfChatroom(User user, Chatroom chatroom) throws NotFoundException {
		List<Membership> memberships = user.getMemberships();
		// search for the chatroom in the user's memberships
		for (Membership m : memberships) {
			// if found, return the membership
			if (m.getChatroom() == chatroom) {
				return m;
			}
		}
		// if not found, throw an exception
		throw new NotFoundException("User is not a member of the chatroom " + chatroom.getChatroomName());
	}

	/**
	 * Create a member_of relation between user and chatroom
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void createMemberRelation(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getMemberOfChatrooms();
		List<User> users = chatroom.getMembers();
		List<Membership> memberships = user.getMemberships();

		// add the chatroom to user's list of chatrooms he is member of
		chatrooms.add(chatroom);
		// add user to chatrooms list of members
		users.add(user);
		// create a membership relation entity for the user
		Membership membership = new Membership(user, chatroom);
		memberships.add(membership);

		// save the chatroom, and its relations
		chatroomRepository.save(chatroom);
		// save the user to preserve relations
		userRepository.save(user);
	}

	/**
	 * Create a admin_of relation between user and chatroom
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void createAdminRelation(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getAdminOfChatrooms();
		List<User> users = chatroom.getAdministrators();

		chatrooms.add(chatroom);
		users.add(user);
		// save the chatroom, and its relations
		chatroomRepository.save(chatroom);
	}

	/**
	 * create INVITES relation from chatroom to user
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void createMemberInvitation(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getChatroomInvites();
		List<User> users = chatroom.getMemberInvitees();
		// create the relation
		chatrooms.add(chatroom);
		users.add(user);
		// save the chatroom, and its relations
		chatroomRepository.save(chatroom);
	}

	/**
	 * create INVITES relation from chatroom to user
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void createAdminInvitation(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getChatroomAdminInvites();
		List<User> users = chatroom.getAdminInvitees();
		// create the relation
		chatrooms.add(chatroom);
		users.add(user);
		// save the chatroom, and its relations
		chatroomRepository.save(chatroom);
	}

	/**
	 * delete INVITES relation from chatroom to user, if it exists
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void deleteMemberInvitation(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getChatroomInvites();
		List<User> users = chatroom.getMemberInvitees();
		// if the user has received invite
		if (memberInvitationSent(user, chatroom)) {
			// delete the relation
			chatrooms.remove(chatroom);
			users.remove(user);
			// save the chatroom, and update its relations
			chatroomRepository.save(chatroom);
		}
	}

	/**
	 * delete ADMIN_INVITES relation from chatroom to user, if it exists
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void deleteAdminInvitation(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getChatroomAdminInvites();
		List<User> users = chatroom.getAdminInvitees();
		// if the user has received invite
		if (adminInvitationSent(user, chatroom)) {
			// delete the relation
			chatrooms.remove(chatroom);
			users.remove(user);
			// save the chatroom, and update its relations
			chatroomRepository.save(chatroom);
		}
	}

	/**
	 * delete MEMBER_OF relation from user to chatroom, if it exists
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void deleteMembership(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getMemberOfChatrooms();
		List<User> users = chatroom.getMembers();
		List<Membership> memberships = user.getMemberships();
		// if the user has received invite
		if (isMember(user, chatroom)) {
			// delete the relation
			chatrooms.remove(chatroom);
			users.remove(user);
			// delete the membership
			// delete the membership
			for (Membership m : memberships) {
				if (m.getChatroom() == chatroom) {
					memberships.remove(m);
					break;
				}
			}
			// save the chatroom, and update its relations
			chatroomRepository.save(chatroom);
		}
	}

	/**
	 * delete MEMBER_OF relation from user to chatroom, if it exists
	 * 
	 * @param user
	 * @param chatroom
	 */
	private void deleteAdminship(User user, Chatroom chatroom) {
		List<Chatroom> chatrooms = user.getAdminOfChatrooms();
		List<User> users = chatroom.getAdministrators();
		// if the user has received invite
		if (isAdmin(user, chatroom)) {
			// delete the relation
			chatrooms.remove(chatroom);
			users.remove(user);
			// save the chatroom, and update its relations
			chatroomRepository.save(chatroom);
		}
	}

	// transfer owner, bad user, unban user, isbanned
}
