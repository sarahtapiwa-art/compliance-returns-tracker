package zw.co.nbs.returnstrackingsystem.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 10:05
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response for messages")
public class MessageResponse {

    @Schema(
            description = "Response message indicating the result",
            example = "Submission sent successfully via your Outlook account"
    )
    private String message;

    @Schema(
            description = "Unique tracking ID for this operation",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @Builder.Default
    private String trackingId = UUID.randomUUID().toString();

    @Schema(
            description = "Timestamp when the response was generated",
            example = "2024-01-15T10:30:00+02:00"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Schema(
            description = "HTTP status code",
            example = "200"
    )
    private Integer statusCode;

    @Schema(
            description = "Whether the operation was successful",
            example = "true"
    )
    private Boolean success;

    @Schema(
            description = "Additional details or error information"
    )
    private Object details;

    // Convenience constructors
    public MessageResponse(String message) {
        this.message = message;
        this.trackingId = UUID.randomUUID().toString();
        this.timestamp = OffsetDateTime.now();
        this.success = true;
    }

    public MessageResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.trackingId = UUID.randomUUID().toString();
        this.timestamp = OffsetDateTime.now();
    }

    public MessageResponse(String message, Integer statusCode, Boolean success) {
        this.message = message;
        this.statusCode = statusCode;
        this.success = success;
        this.trackingId = UUID.randomUUID().toString();
        this.timestamp = OffsetDateTime.now();
    }

    // Factory methods for common responses
    public static MessageResponse success(String message) {
        return MessageResponse.builder()
                .message(message)
                .success(true)
                .statusCode(200)
                .build();
    }

    public static MessageResponse success(String message, Object details) {
        return MessageResponse.builder()
                .message(message)
                .success(true)
                .statusCode(200)
                .details(details)
                .build();
    }

    public static MessageResponse error(String message) {
        return MessageResponse.builder()
                .message(message)
                .success(false)
                .statusCode(500)
                .build();
    }

    public static MessageResponse error(String message, Integer statusCode) {
        return MessageResponse.builder()
                .message(message)
                .success(false)
                .statusCode(statusCode)
                .build();
    }

    public static MessageResponse error(String message, Integer statusCode, Object details) {
        return MessageResponse.builder()
                .message(message)
                .success(false)
                .statusCode(statusCode)
                .details(details)
                .build();
    }
    public static MessageResponse outlookEmailSent(String recipientEmail) {
        return MessageResponse.builder()
                .message(String.format("Email sent successfully to %s via Outlook", recipientEmail))
                .success(true)
                .statusCode(200)
                .build();
    }

    // Predefined common responses
    public static MessageResponse emailSentSuccess() {
        return success("Email sent successfully via Outlook");
    }

    public static MessageResponse emailSentSuccess(String recipient) {
        return success(String.format("Email sent successfully to %s", recipient));
    }

    public static MessageResponse emailSentSuccess(String recipient, String messageId) {
        return MessageResponse.builder()
                .message(String.format("Email sent successfully to %s", recipient))
                .success(true)
                .statusCode(200)
                .details(
                        EmailSendDetails.builder()
                                .messageId(messageId)
                                .recipient(recipient)
                                .build()
                       )
                .build();
    }

    public static MessageResponse tokenExpired() {
        return error("Authentication token has expired. Please log in again.", 401);
    }

    public static MessageResponse permissionDenied() {
        return error("You don't have permission to perform this action.", 403);
    }

    public static MessageResponse notFound(String resource) {
        return error(String.format("%s not found", resource), 404);
    }

    public static MessageResponse invalidEmail(String email) {
        return error(String.format("Invalid email address: %s", email), 400);
    }

    // Inner class for email details
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmailSendDetails {
        @Schema(description = "Microsoft Graph message ID", example = "AAMkAGNjOTY4ZWMzLTI3...")
        private String messageId;

        @Schema(description = "Recipient email address", example = "regulator@gov.org")
        private String recipient;

        @Schema(description = "Sender email address", example = "user@company.com")
        private String sender;

        @Schema(description = "Email subject", example = "Submission Notice - Q4 Report")
        private String subject;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @Schema(description = "Time when email was sent")
        private OffsetDateTime sentAt;
    }

    // Builder pattern for fluent API (already provided by Lombok @Builder)
    // But we can add custom builder methods

    public static class MessageResponseBuilder {
        private String trackingId = UUID.randomUUID().toString();
        private OffsetDateTime timestamp = OffsetDateTime.now();

        public MessageResponseBuilder withDetails(Object details) {
            this.details = details;
            return this;
        }

        public MessageResponseBuilder withStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }
    }
}