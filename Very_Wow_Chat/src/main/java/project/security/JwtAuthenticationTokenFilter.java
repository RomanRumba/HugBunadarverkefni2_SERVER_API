package project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import project.persistance.entities.JwtAuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Róman(ror9@hi.is)
 */
public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

	public JwtAuthenticationTokenFilter() {
		super("/auth/**");
	}

	/** This is where the JWT tokens will be decoded and authenticated */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
		// The token should be stored in the header under " Authorization "
		String header = httpServletRequest.getHeader("Authorization");

		/*
		 * if Authorization was not found or if the token dosen't start with "Token" we
		 * pass an error.
		 * 
		 * Its a good practice just to add something to the token just to see at least
		 * if it is our token to avoid unnecessary computation
		 */
		if (header == null || !header.startsWith("Token ")) {
			throw new RuntimeException("JWT Token is missing");
		}

		/*
		 * We need to remember that the token starts with "Token xxxxxxx" so we grab the
		 * token part for validation
		 */
		String authenticationToken = header.substring(6);

		JwtAuthenticationToken token = new JwtAuthenticationToken(authenticationToken);
		// let our authentication manager handle all the token next
		return getAuthenticationManager().authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
		chain.doFilter(request, response);
	}
}
