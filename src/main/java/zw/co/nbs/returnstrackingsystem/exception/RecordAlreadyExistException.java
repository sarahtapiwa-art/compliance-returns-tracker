package zw.co.nbs.returnstrackingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * createdBy romeo
 * createdDate 27/8/2025
 * createdTime 09:35
 * projectName compliance-returns-tracker
 **/

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecordAlreadyExistException extends RuntimeException{
    public RecordAlreadyExistException(String message) {
        super(message);
    }
}
