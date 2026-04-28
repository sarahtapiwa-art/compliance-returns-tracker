package zw.co.nbs.returnstrackingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * createdBy romeo
 * createdDate 10/2/2026
 * createdTime 08:49
 * projectName compliance-returns-tracker
 **/

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
