package returnstrackingsystem.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import returnstrackingsystem.domain.enums.SubmissionStatus;
import java.time.OffsetDateTime;

@Value
@Builder
@Schema(description = "Submission dashboard item response")
public class SubmissionDashboardResponse {

    @Schema(description = "Unique identifier of the submission", example = "12345")
    Long id;

    @Schema(description = "Title of the submission", example = "Q4 Financial Report")
    String title;

    @Schema(description = "Department responsible for the submission", example = "Finance")
    String department;

    @Schema(description = "Due date and time of the submission", example = "2024-03-31T23:59:59Z")
    OffsetDateTime dueAt;

    @Schema(description = "Current status of the submission",
            allowableValues = {"PENDING", "SUBMITTED", "APPROVED", "REJECTED", "OVERDUE"},
            example = "PENDING")
    SubmissionStatus status;

    @Schema(description = "Label representing the reporting period", example = "Q4 2024")
    String periodLabel;
}