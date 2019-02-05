package project.payloads;

import java.util.HashMap;
import java.util.Map;

import project.persistance.entities.User;

/**
 * This class is for wrapping data in JSON objects
 * 
 * @author Vilhelml
 * @since 20.10.18
 */
public class UserFullResponder {
	private String username;
	private String displayName;
	private String email;
	private Long created;

	public UserFullResponder(User user) {
		this.username = user.getUsername();
		this.displayName = user.getDisplayName();
		this.email = user.getEmail();
		this.created = user.getCreated();
	}

	/**
	 * constructor notað af spring controller til að vinna með json objects
	 * 
	 * @param username
	 * @param displayName
	 * @param email
	 */
	public UserFullResponder(String username, String displayName, String email) {
		this.username = username;
		this.displayName = displayName;
		this.email = email;
	}

	/**
	 * wrap the response
	 * 
	 * @return wrapped response
	 */
	public Object wrapResponse() {
		Map<String, UserFullResponder> wrapper = new HashMap<>();
		wrapper.put("GoodResp", this);
		return wrapper;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmail() {
		return email;
	}

	public Long getCreated() {
		return created;
	}

}
