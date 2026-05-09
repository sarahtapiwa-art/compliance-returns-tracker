package returnstrackingsystem.exception;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import returnstrackingsystem.dtos.response.ErrorResponse;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandling extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvocationTargetException.class)
    public ResponseEntity<String> handleInvocationTargetException(InvocationTargetException ex) {
        Throwable targetException = ex.getCause();
        System.err.println("=== INVOCATION TARGET EXCEPTION DETAILS ===");
        System.err.println("Target exception: " + targetException.getClass().getName());
        System.err.println("Message: " + targetException.getMessage());
        targetException.printStackTrace();

        return ResponseEntity.status(500)
                .body("Error: " + targetException.getClass().getSimpleName() + " - " + targetException.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField,
                        FieldError::getDefaultMessage));

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                Map.of("errors", validationErrors)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({
            RecordNotFoundException.class
    })
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNotFound(RecordNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "RECORD_NOT_FOUND",
                e.getMessage(),
                null
        );
        return new ResponseEntity<>(errorResponse, NOT_FOUND);
    }

    @ExceptionHandler({
            FileNotFoundException.class
    })
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "RECORD_NOT_FOUND",
                e.getMessage(),
                null
        );
        return new ResponseEntity<>(errorResponse, NOT_FOUND);
    }

    @ExceptionHandler({
            BadRequestException.class,
            RecordAlreadyExistException.class,
            InvalidEmailException.class,
            RuntimeException.class,
            BusinessRuleException.class
    })
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_REQUEST",
                e.getMessage(),
                null
        );
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support.",
                null
        );
        return new ResponseEntity<>(errorResponse, INTERNAL_SERVER_ERROR);
    }
}