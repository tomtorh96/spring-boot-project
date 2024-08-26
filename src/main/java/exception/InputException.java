package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InputException extends RuntimeException {

	private static final long serialVersionUID = 3130902187796700161L;

	public InputException() {
		super();
	}

	public InputException(String message) {
		super(message);
		
	}

	public InputException(Throwable cause) {
		super(cause);
		
	}

	public InputException(String message, Throwable cause) {
		super(message, cause);
		
	}


}
