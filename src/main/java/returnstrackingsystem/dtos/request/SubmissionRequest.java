package returnstrackingsystem.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a submission")
public class SubmissionRequest {

    @NotNull(message = "Period start is mandatory")
    @Schema(description = "Start of the submission period", example = "2025-08-01T00:00")
    private LocalDateTime periodStart;

    @NotNull(message = "Period end is mandatory")
    @Schema(description = "End of the submission period", example = "2025-08-31T23:59")
    private LocalDateTime periodEnd;

    @NotNull(message = "Due date is mandatory")
    @Schema(description = "Submission due date", example = "2025-09-05T17:00")
    private LocalDateTime dueAt;
}


