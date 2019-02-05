package project.payloads;

/**
 * This class has several to purposes 1- this act as a payload (BOTH FROM POST
 * REQUESTS AND AS MIDDLEWARES) 2- later we will use this to create a JWT The
 * reason i am not using Users.js is because we only need 2 variables and
 * User.js has so much that we would have to implement ALLOT OF LAZY loading and
 * i think its just to much so we have this class to make it more simple
 * @author Róman(ror9@hi.is)
 */
public class JwtUser {

	private String userName;
	private String password;

	/**
	 * this is for authentication, we can have role based authentication later if we
	 * want to add more restrictions to what people can and cant do like user roles
	 * and admin roles but for now we just have one role
	 */
	private String role;

	public JwtUser() {
	}

	public JwtUser(String userName, String password) {
		this.userName = userName;
		this.password = password;
		this.role = "User"; // the moment everyone have user role later we can change that if we want
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUserName() {
		return userName;
	}

	public String getRole() {
		return role;
	}

	public String getPassword() {
		return this.password;
	}
}
