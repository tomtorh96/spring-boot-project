package superapp.command;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class CommandDeprecationException extends RuntimeException {

	private static final long serialVersionUID = 6006477409908600221L;

	public CommandDeprecationException() {
		super();
	}

	public CommandDeprecationException(String message) {
		super(message);
	}

	public CommandDeprecationException(Throwable cause) {
		super(cause);
	}

	public CommandDeprecationException(String message, Throwable cause) {
		super(message, cause);
	}

}
