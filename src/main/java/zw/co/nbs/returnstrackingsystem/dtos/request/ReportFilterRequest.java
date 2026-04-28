package zw.co.nbs.returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "Filter criteria for generating reports")
public class ReportFilterRequest {

    @Schema(
            description = "Department name to filter by",
            example = "Finance"
    )
    private String department;

    @Schema(
            description = "Submission status to filter by (e.g., PENDING, SUBMITTED, OVERDUE)",
            example = "PENDING"
    )
    private String status;

    @Schema(
            description = "Start date/time for filtering submissions",
            type = "string",
            format = "date-time",
            example = "2025-01-01T00:00:00Z"
    )
    private OffsetDateTime fromDate;

    @Schema(
            description = "End date/time for filtering submissions",
            type = "string",
            format = "date-time",
            example = "2025-01-31T23:59:59Z"
    )
    private OffsetDateTime toDate;
}
