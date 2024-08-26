package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class NoPremissionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -785864403861363866L;

	public NoPremissionException() {
		super();
	}

	public NoPremissionException(String message) {
		super(message);

	}

	public NoPremissionException(Throwable cause) {
		super(cause);

	}

	public NoPremissionException(String message, Throwable cause) {
		super(message, cause);

	}

}
