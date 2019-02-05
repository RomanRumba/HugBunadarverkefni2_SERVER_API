package project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import project.payloads.JwtUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * @author Róman(ror9@hi.is)
 * */
@Component
public class JwtGenerator {
	// get our secret key from the environmental variables
	@Value("${cryptography.security.password}")
	private String secretKey;

	/**
	 * Pretty simple we create a JWT token here to return to the client so he can
	 * use it to authenticate himself next time.
	 */
	public String generate(JwtUser jwtUser) {
		Claims claims = Jwts.claims().setSubject(jwtUser.getUserName());
		claims.put("role", jwtUser.getRole());
		String base64EncodedSecretKey = TextCodec.BASE64.encode(secretKey);
		return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, base64EncodedSecretKey).compact();
	}
}
