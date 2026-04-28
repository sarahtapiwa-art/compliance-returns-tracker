package zw.co.nbs.returnstrackingsystem.exception;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 08:52
 * projectName compliance-returns-tracker
 **/

public class EmailSendException extends RuntimeException{
    public EmailSendException(String message) {
        super(message);
    }
}
