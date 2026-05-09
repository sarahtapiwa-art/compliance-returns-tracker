package returnstrackingsystem.exception;

/**
 * createdBy romeo
 * createdDate 28/10/2025
 * createdTime 14:40
 * projectName compliance-returns-tracker
 **/

public class FileSizeExceededException extends RuntimeException{
    public FileSizeExceededException(String message) {
        super(message);
    }
}
