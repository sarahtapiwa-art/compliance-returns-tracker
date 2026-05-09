package returnstrackingsystem.exception;

import lombok.Getter;

/**
 * createdBy romeo
 * createdDate 10/11/2025
 * createdTime 14:36
 * projectName compliance-returns-tracker
 **/

@Getter
public class ValidationException extends RuntimeException {
    private final String field;

    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
    }
}
