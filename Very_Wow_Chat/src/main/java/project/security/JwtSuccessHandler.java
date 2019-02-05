package project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @author Róman(ror9@hi.is)
 * */
public class JwtSuccessHandler implements AuthenticationSuccessHandler {

	/**
	 * We can implement some kind of success addition if needed here but for now it
	 * will stay empty
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Authentication authentication) throws IOException, ServletException {
	}
}
