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
public class UserResponder {
	private String username;
	private String displayName;

	public UserResponder(User user) {
		this.username = user.getUsername();
		this.displayName = user.getDisplayName();
	}
	
	/**
	 * constructor notað af spring controller til að vinna með json objects
	 * 
	 * @param username
	 * @param displayName
	 */
	public UserResponder(String username, String displayName) {
		this.username = username;
		this.displayName = displayName;
	}

	/**
	 * wrap the response
	 * 
	 * @return wrapped response
	 */
	public Object wrapResponse() {
		Map<String, UserResponder> wrapper = new HashMap<>();
		wrapper.put("GoodResp", this);
		return wrapper;
	}

	// getters

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

}
