package project.errors;

import org.springframework.http.HttpStatus;

/**
 * A custom exception class for throwing and catching unauthorized errors and returning 401 status
 * 
 * @author Vilhelml
 */
public class UnauthorizedException extends HttpException {

	private static final long serialVersionUID = -3792131199243047779L;

	public UnauthorizedException(String msg) {
		super(msg);
		super.setStatus(HttpStatus.UNAUTHORIZED);
	}
}
