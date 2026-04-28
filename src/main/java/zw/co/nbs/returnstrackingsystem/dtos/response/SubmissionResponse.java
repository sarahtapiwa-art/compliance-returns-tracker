package zw.co.nbs.returnstrackingsystem.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zw.co.nbs.returnstrackingsystem.domain.enums.SubmissionStatus;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a submission")
public class SubmissionResponse {

    @Schema(description = "Unique identifier of the submission", example = "1")
    private Long id;

    @Schema(description = "Return definition associated with this submission")
    private ReturnDefinition returnDefinition;

    @Schema(description = "Start date of the submission period", example = "2025-08-01T00:00:00Z")
    private OffsetDateTime periodStart;

    @Schema(description = "End date of the submission period", example = "2025-08-31T23:59:59Z")
    private OffsetDateTime periodEnd;

    @Schema(description = "Due date for the submission", example = "2025-09-05T17:00:00Z")
    private OffsetDateTime dueAt;

    @Schema(description = "Current status of the submission")
    private SubmissionStatus status;

    @Schema(description = "Flag to indicate if the submission is deleted", example = "false")
    private Boolean deleted;
}
