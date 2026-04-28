package zw.co.nbs.returnstrackingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * createdBy romeo
 * createdDate 25/8/2025
 * createdTime 08:42
 * projectName compliance-returns-tracker
 **/

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEmailException extends RuntimeException{
    public InvalidEmailException(String message) {
        super(message);
    }
}
