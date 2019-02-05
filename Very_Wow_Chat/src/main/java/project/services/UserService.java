package project.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.errors.BadRequestException;
import project.errors.NotFoundException;
import project.persistance.entities.User;
import project.persistance.repositories.UserRepository;

/**
 * This servies handles functionality relating to users and the users' relations
 * with other users.
 * 
 * @author Vilhelml
 *
 */
@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	/**
	 * Updates user with user name <code>username</code>. Use <code>null</code> for
	 * those properties you don't want to update.
	 * 
	 * @param user The user
	 * @param newDisplayName Display name of user.
	 * @param newEmail Email of user (unencrypted?)
	 * @param newPassword Password (unencrypted/unhashed) ?
	 * 
	 * @throws NotFoundException
	 */
	public void updateUser(User user, String newDisplayName, String newEmail, String newPassword)
			throws NotFoundException {

		BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();

		if (newDisplayName != null) {
			user.setDisplayName(newDisplayName);
		}

		if (newEmail != null) {
			// NOTE: encrypt email here.
			String emailEncrypted = CryptographyService.getCiphertext(newEmail);
			user.setEmail(emailEncrypted);
		}

		if (newPassword != null) {
			String passwordDigest = bcpe.encode(newPassword);
			user.setPassword(passwordDigest);
		}

		saveUser(user);
	}

	/**
	 * Check if a user exists with a given username and is active
	 * 
	 * @param username User's user name.
	 * 
	 * @return <code>true</code> if user name is in use, else <code>false</code>.
	 */
	public Boolean userExistsAndActive(String username) {
		User user = this.userRepository.findByUsername(username);
		if (user != null && user.getIsActive()) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the user name is available.
	 * 
	 * @param username User's user name.
	 * 
	 * @return <code>true</code> if the user name is taken / in use, otherwise
	 *         <code>false</code>.
	 */
	public Boolean usernameTaken(String username) {
		User user = this.userRepository.findByUsername(username);
		return user != null;
	}

	/**
	 * save a user, used to apply updates
	 * 
	 * NOTE: email of user is assumed to be encrypted. (or is it?)
	 * 
	 * @param user the user to be updated
	 * 
	 * @return
	 */
	public User saveUser(User user) {
		// save the user in database
		return userRepository.save(user);
	}

	/**
	 * create a a user
	 * 
	 * NOTE: assumes email of <code>newUser</code> is encrypted.
	 * 
	 * @param newUser
	 * 
	 * @return the new user
	 * @throws BadRequestException if username is taken
	 */
	public User createUser(User newUser) throws BadRequestException {

		// throw error if username is taken
		if (usernameTaken(newUser.getUsername())) {
			throw new BadRequestException("Username is already in use.");
		}
		User user = userRepository.save(newUser);
		return user;
	}

	/**
	 * returns a user if the username is in use else, returns and error message
	 * 
	 * @param username
	 * 
	 * @return the user
	 * @throws NotFoundException if userName doesn't belong to any user
	 */
	@Transactional(readOnly = true)
	public User findByUsername(String username) throws NotFoundException {
		// throw error if user doesn't exist
		if (!userExistsAndActive(username)) {
			throw new NotFoundException("User not found");
		}

		User user = this.userRepository.findByUsername(username);

		return user;
	}

	/**
	 * disable the user and delete all their relations
	 * 
	 * @param user: user to be deleted
	 */
	@Transactional(readOnly = false)
	public void deleteUser(User user) {
		// disable the user
		user.setIsActive(false);
		String username = user.getUsername();

		// delete all the user's relations
		this.userRepository.deleteUserRelations(username);
		userRepository.save(user);
	}

	/**
	 * Add a friend: sends a friend request, or creates a friend relation if
	 * requestee has already sent a friend request
	 * 
	 * @param requestor: user sending the request
	 * @param requestee user receiving the request
	 * 
	 * @throws BadRequestException
	 */
	@Transactional(readOnly = false)
	public void addFriend(User requestor, User requestee) throws BadRequestException {
		// check if user is sending himself a friend request
		if (requestor == requestee) {
			throw new BadRequestException("Cannot add self as friend.");
		}
		// check if a friend request has already been sent
		if (friendRequestSent(requestor, requestee)) {
			throw new BadRequestException("A friend request is already pending.");
		}
		// check if they are already friends
		if (areFriends(requestor, requestee)) {
			throw new BadRequestException("You are already friends");
		}
		// check if a friend requet has been sent in the other direction already
		if (friendRequestSent(requestee, requestor)) {
			// both users have sent each other a friend request, they are now friends
			// delete the old friend request
			deleteFriendRequest(requestee, requestor);
			// create friend relation
			createFriendRelation(requestee, requestor);
			return;
		}
		// send a friend request
		sendFriendRequest(requestor, requestee);
	}

	/**
	 * Delete a friend request from requestor to requestee if exists
	 * 
	 * @param requestor: the original requestor of the request
	 * @param requestee: the original requestee of the request
	 */
	@Transactional(readOnly = false)
	public void deleteFriendRequest(User requestor, User requestee) {
		List<User> requestorRequestees = requestor.getFriendRequestees();
		List<User> requesteeRequestors = requestee.getFriendRequestors();

		if (friendRequestSent(requestor, requestee)) {
			// delete the request
			requestorRequestees.remove(requestee);
			requesteeRequestors.remove(requestor);
			// save both users so the database will be updated and the request deleted
			userRepository.save(requestee);
			userRepository.save(requestor);
		}
	}

	/**
	 * Delete a friend relation between 2 users
	 * 
	 * @param user1
	 * @param user2
	 */
	@Transactional(readOnly = false)
	public void deleteFriendship(User user1, User user2) throws NotFoundException {
		if (!areFriends(user1, user2)) {
			throw new NotFoundException("There is no friend relation to delete");
		}
		List<User> user1Friends = user1.getFriends();
		List<User> user2Friends = user2.getFriends();

		// remove the users from each other's friendlist
		user1Friends.remove(user2);
		user2Friends.remove(user1);
		// save both users so the database will be updated and the friendship was
		// deleted
		userRepository.save(user1);
		userRepository.save(user2);
	}

	/**
	 * Checks if user1 and user2 are friends
	 * 
	 * @param user1
	 * @param user2
	 * @return true if they are friends, else returns false
	 */
	@Transactional(readOnly = false)
	public Boolean areFriends(User user1, User user2) {
		List<User> user1Friends = user1.getFriends();
		List<User> user2Friends = user2.getFriends();

		// data invariability: if the former condition is true, then the latter should
		// also be,
		// if the former is false, then the latter should also be
		return user1Friends.contains(user2) && user2Friends.contains(user1);
	}

	/**
	 * Checks requestor has sent requestee a friend request
	 * 
	 * @param requestor: user sending the request
	 * @param requestee user receiving the request
	 * @return true if a friend request is pending, else returns false
	 */
	@Transactional(readOnly = false)
	public Boolean friendRequestSent(User requestor, User requestee) {
		List<User> requestorRequestees = requestor.getFriendRequestees();
		List<User> requesteeRequestors = requestee.getFriendRequestors();

		// data invariability: if the former condition is true, then the latter should
		// also be,
		// if the former is false, then the latter should also be
		return requestorRequestees.contains(requestee) && requesteeRequestors.contains(requestor);
	}

	/**
	 * Send a friend request
	 * 
	 * @param requestor: user sending the request
	 * @param requestee user receiving the request
	 * @throws BadRequestException if a request is already pending
	 */
	protected void sendFriendRequest(User requestor, User requestee) throws BadRequestException {
		// get the requestors and requestees
		List<User> requestorRequestees = requestor.getFriendRequestees();
		List<User> requesteeRequestors = requestee.getFriendRequestors();

		// check if a friend request has already been sent
		if (friendRequestSent(requestor, requestee)) {
			throw new BadRequestException("A friend request is already pending.");
		}

		// create the friend request
		requestorRequestees.add(requestee);
		requesteeRequestors.add(requestor);

		// save the relation in database
		userRepository.save(requestee);
		userRepository.save(requestor);
	}

	/**
	 * Create a friend relation between 2 users
	 * 
	 * @param user1
	 * @param user2
	 * @throws BadRequestException: if users are already friends or if it's the same
	 *                              user
	 */
	protected void createFriendRelation(User user1, User user2) throws BadRequestException {
		if (user1 == user2) {
			throw new BadRequestException("Cannot add self as friend.");
		}
		if (areFriends(user1, user2)) {
			throw new BadRequestException("Users are already friends.");
		}
		List<User> user1Friends = user1.getFriends();
		List<User> user2Friends = user2.getFriends();

		user1Friends.add(user2);
		user2Friends.add(user1);

		// save the relation in database
		userRepository.save(user1);
		userRepository.save(user2);
	}
}
