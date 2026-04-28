package zw.co.nbs.returnstrackingsystem.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a schedule rule")
public class ScheduleRuleRequest {

    @NotNull(message = "ReturnDefinition id is required")
    @Schema(description = "ID of the return definition associated with this schedule rule", example = "1")
    private Long returnDefinitionId;

    @NotNull(message = "Days to remind required")
    @Schema(description = "Number of days before the deadline to send a reminder", example = "3")
    private Integer remindDaysBefore;

    @NotNull(message = "Escalation hours required")
    @Schema(description = "Number of hours after the reminder to escalate if not completed", example = "24")
    private Integer escalateAfterHours;
}

