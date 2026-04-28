package zw.co.nbs.returnstrackingsystem.exception;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 08:25
 * projectName compliance-returns-tracker
 **/

public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }

    public AuthenticationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
