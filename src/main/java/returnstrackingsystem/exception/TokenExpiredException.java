package returnstrackingsystem.exception;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 08:26
 * projectName compliance-returns-tracker
 **/

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}