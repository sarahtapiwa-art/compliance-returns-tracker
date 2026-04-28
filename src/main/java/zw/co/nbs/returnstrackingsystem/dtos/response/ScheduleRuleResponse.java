package zw.co.nbs.returnstrackingsystem.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;

/**
 * createdBy       lorraine.mhizha
 * createdDate     25/8/2025
 * createdTime     12:01
 * projectName     compliance-returns-tracker
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for schedule rules")
public class ScheduleRuleResponse {

    @Schema(description = "Unique identifier of the schedule rule", example = "1")
    private Long id;

    @Schema(description = "Return definition associated with this schedule rule")
    private ReturnDefinition returnDefinition;

    @Schema(description = "Number of days before submission to send reminder", example = "3")
    private Integer remindDaysBefore;

    @Schema(description = "Number of hours after which escalation is triggered", example = "24")
    private Integer escalateAfterHours;

    @Schema(description = "Email address to receive escalation notifications", example = "manager@nbs.co.zw")
    private String escalationEmail;
}

