package zw.co.nbs.returnstrackingsystem.dtos.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * createdBy romeo
 * createdDate 18/8/2025
 * createdTime 20:33
 * projectName compliance-returns-tracker
 **/

@Data
@Schema(description = "Standard structure for API error responses")
public class ErrorResponse {

    @Schema(description = "Error code representing the type of error", example = "SUBMISSION_NOT_FOUND")
    private String code;

    @Schema(description = "Human-readable error message", example = "Submission not found for the given ID")
    private String message;

    @Schema(description = "Optional additional details about the error")
    private Map<String, Object> details;

    @Schema(description = "Timestamp when the error occurred", example = "2025-08-25T14:30:00Z")
    private Instant timestamp;

    public ErrorResponse(String code, String message, Map<String, Object> details) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = Instant.now();
    }
}

