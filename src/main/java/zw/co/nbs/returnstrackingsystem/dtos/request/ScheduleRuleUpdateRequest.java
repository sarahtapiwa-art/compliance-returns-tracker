package zw.co.nbs.returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * createdBy romeo
 * createdDate 28/10/2025
 * createdTime 10:24
 * projectName compliance-returns-tracker
 **/

@Data
@Schema(description = "Request object for updating a schedule rule")
public class ScheduleRuleUpdateRequest {
    @Schema(description = "ID of the return definition associated with this schedule rule", example = "1")
    private Long returnDefinitionId;

    @Schema(description = "Number of days before the deadline to send a reminder", example = "3")
    private Integer remindDaysBefore;

    @Schema(description = "Number of hours after the reminder to escalate if not completed", example = "24")
    private Integer escalateAfterHours;
}
