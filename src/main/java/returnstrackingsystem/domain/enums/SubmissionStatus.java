package returnstrackingsystem.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current status of a submission")
public enum SubmissionStatus {

    @Schema(description = "Submission is pending and not yet processed")
    PENDING,

    @Schema(description = "Submission has been uploaded but not yet sent")
    UPLOADED,

    @Schema(description = "Submission has been sent to the regulator")
    SUBMITTED,

    @Schema(description = "Submission is overdue")
    OVERDUE,

    @Schema(description = "Submission has been uploaded and is overdue")
    UPLOADED_OVERDUE,

    @Schema(description = "Submission process is completed and closed")
    CLOSED
}
