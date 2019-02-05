package project.payloads;

import project.persistance.entities.User;

/**
 * This class is is for receiving update user requests
 * 
 * @author Vilhelml
 * @since 20.10.18
 */
public class UserUpdateReceiver {

	private String password;
	private String displayName;
	private String email;

	public UserUpdateReceiver(User user) {
		this.password = user.getPassword();
		this.displayName = user.getDisplayName();
		this.email = user.getEmail();
	}

	/**
	 * constructor notað af spring controller til að vinna með json objects
	 * 
	 * @param password
	 * @param displayName
	 * @param email
	 */
	public UserUpdateReceiver(String password, String displayName, String email) {
		this.password = password;
		this.displayName = displayName;
		this.email = email;
	}

	// getters

	public String getPassword() {
		return password;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmail() {
		return email;
	}

}
