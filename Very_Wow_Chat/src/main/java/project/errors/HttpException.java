package project.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import project.payloads.ErrorResponder;

/**
 * A parent to various exceptions representing to common http errors the
 * children exception classes that extend this class need to initalize the http
 * status
 * 
 * @author Vilhelml
 */
public abstract class HttpException extends Exception {
	
	private static final long serialVersionUID = -6607734661793333734L;

	private HttpStatus status;

	public HttpException(String msg) {
		super(msg);
	}

	public ResponseEntity<Object> getErrorResponseEntity() {
		ErrorResponder body = new ErrorResponder();
		body.setError(this.getMessage());
		return new ResponseEntity<>(body.getWrappedError(), this.status);
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}
}
