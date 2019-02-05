package project.payloads;

/**
 * Class to receive JSON object from client.
 */
public class LoginFormReciver {

	// The username and password received
	private final String username;
	private final String password;

	/* IMPLEMENT XSS */
	public LoginFormReciver(String us, String pass) {
		this.username = us;
		this.password = pass;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
