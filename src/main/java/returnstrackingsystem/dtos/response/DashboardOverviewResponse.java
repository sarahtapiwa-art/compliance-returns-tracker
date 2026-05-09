package returnstrackingsystem.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Dashboard overview statistics response")
public class DashboardOverviewResponse {

    @Schema(description = "Total number of submissions", example = "150")
    long totalSubmissions;

    @Schema(description = "Number of completed submissions", example = "120")
    long completedSubmissions;

    @Schema(description = "Number of overdue submissions", example = "15")
    long overdueSubmissions;

    @Schema(description = "Number of upcoming submissions", example = "15")
    long upcomingSubmissions;

    @Schema(description = "Completion rate as a percentage (0-100)", example = "80.0")
    double completionRate;
}