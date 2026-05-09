package returnstrackingsystem.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the submission completion rate for a department")
public record CompletionRateResponse(@Schema(
        description = "Department name",
        example = "Finance"
) String department, @Schema(
        description = "Completion rate as a percentage (0–100)",
        example = "87.5"
) double completionRate) {

}
