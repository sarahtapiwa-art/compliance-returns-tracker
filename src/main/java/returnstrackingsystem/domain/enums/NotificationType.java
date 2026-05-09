package returnstrackingsystem.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of notification to be sent")
public enum NotificationType {

    @Schema(description = "A reminder notification")
    REMINDER,

    @Schema(description = "An escalation notification")
    ESCALATION
}
