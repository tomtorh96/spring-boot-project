package exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
 static final long serialVersionUID = 1727463846876787902L;

	public UnauthorizedException() {
		super();
	}

	public UnauthorizedException(String message) {
		super(message);
		
	}

	public UnauthorizedException(Throwable cause) {
		super(cause);
		
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
		
	}

	

}
