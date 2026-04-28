package zw.co.nbs.returnstrackingsystem.exception;

/**
 * createdBy romeo
 * createdDate 28/10/2025
 * createdTime 14:41
 * projectName compliance-returns-tracker
 **/

public class FileTypeNotAllowedException extends RuntimeException{
    public FileTypeNotAllowedException(String message) {
        super(message);
    }
}
