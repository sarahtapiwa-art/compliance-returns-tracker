package zw.co.nbs.returnstrackingsystem.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Frequency options for scheduling or reporting")
public enum Frequency {
    @Schema(description = "Occurs every day")
    DAILY,

    @Schema(description = "Occurs every week")
    WEEKLY,

    @Schema(description = "Occurs every month")
    MONTHLY,

    @Schema(description = "Occurs every quarter")
    QUARTERLY,

    @Schema(description = "Occurs every semi-annual")
    SEMI_ANNUAL,

    @Schema(description = "Occurs every year")
    YEARLY
}
