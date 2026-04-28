package zw.co.nbs.returnstrackingsystem.exception;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 09:00
 * projectName compliance-returns-tracker
 **/

public class PermissionDeniedException extends RuntimeException{
    public PermissionDeniedException(String message) {
        super(message);
    }
}
