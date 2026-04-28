package zw.co.nbs.returnstrackingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:32
 * projectName compliance-returns-tracker
 **/

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialTokenException extends RuntimeException{
    public InvalidCredentialTokenException(String message) {
        super(message);
    }
}
