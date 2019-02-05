package project.persistance.entities;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * This is a container for our JWT tokens.
 * @author Róman(ror9@hi.is)
 */
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = -1826501946064090116L;

	private String token;

	public JwtAuthenticationToken(String token) {
		super(null, null);
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	// We don't need these two functions but still have to override them.

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}
}
