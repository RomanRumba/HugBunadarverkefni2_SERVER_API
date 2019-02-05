package project.payloads;

public class PasswordResetRequest {

	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public PasswordResetRequest() {
	}

	public PasswordResetRequest(String username) {
		super();
		this.username = username;
	}
}
