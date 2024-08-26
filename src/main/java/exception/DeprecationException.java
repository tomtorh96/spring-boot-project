package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class DeprecationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4699349823942509098L;

	public DeprecationException() {
		super();
	}

	public DeprecationException(String message) {
		super(message);
		
	}

	public DeprecationException(Throwable cause) {
		super(cause);
	
	}

	public DeprecationException(String message, Throwable cause) {
		super(message, cause);
		
	}

	

}
