package project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import project.payloads.JwtUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** this class has to validate user jtw tokens that is has been passed 
 * @author Róman(ror9@hi.is)
 * */
@Component
public class JwtValidator {
	// get our secret key from the environmental variables
	@Value("${cryptography.security.password}")
	private String secretKey;

	/**
	 * Validate the JWT token given, if the token is valid a jwtUser will be
	 * returned
	 */
	public JwtUser validate(String token) {

		String base64EncodedSecretKey = TextCodec.BASE64.encode(secretKey);

		JwtUser jwtUser = null;
		try {
			// get the tokens information
			Claims body = Jwts.parser().setSigningKey(base64EncodedSecretKey).parseClaimsJws(token).getBody();
			// create a token user for further validation
			jwtUser = new JwtUser();
			// the data to the user
			jwtUser.setUserName(body.getSubject());
			jwtUser.setRole((String) body.get("role")); // important !!!
		} catch (Exception e) {
			System.out.println(e);
		}

		return jwtUser;
	}
}
