package zw.co.nbs.returnstrackingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zw.co.nbs.returnstrackingsystem.dtos.response.*;
import zw.co.nbs.returnstrackingsystem.service.DashboardService;

import java.security.Principal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard analytics and tracking")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Get system overview",
            description = "Fetch high-level dashboard metrics (totals, summaries, etc.)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved overview",
                            content = @Content(schema = @Schema(implementation = DashboardOverviewResponse.class))
                    )
            }
    )
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview(Principal principal) {
        return ResponseEntity.ok(dashboardService.getOverview(principal));
    }

    @Operation(
            summary = "Get upcoming submissions",
            description = "Retrieve submissions that are due in the next X days (default is 7).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of upcoming submissions",
                            content = @Content(schema = @Schema(implementation = SubmissionDashboardResponse.class))
                    )
            }
    )
    @GetMapping("/upcoming")
    public ResponseEntity<List<SubmissionDashboardResponse>> getUpcoming(
            @Parameter(description = "Number of days ahead to check for upcoming submissions", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(dashboardService.getUpcomingSubmissions(days));
    }

    @Operation(
            summary = "Get overdue submissions",
            description = "Retrieve submissions that are past their due date.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of overdue submissions",
                            content = @Content(schema = @Schema(implementation = SubmissionDashboardResponse.class))
                    )
            }
    )
    @GetMapping("/overdue")
    public ResponseEntity<List<SubmissionDashboardResponse>> getOverdue(Principal currentUser) {
        return ResponseEntity.ok(dashboardService.getOverdueSubmissions(currentUser));
    }

    @Operation(
            summary = "Get completion rates by department",
            description = "Retrieve submission completion percentages grouped by department.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of completion rate stats",
                            content = @Content(schema = @Schema(implementation = CompletionRateResponse.class))
                    )
            }
    )
    @GetMapping("/completion-rate")
    public ResponseEntity<List<CompletionRateResponse>> getCompletionRates() {
        return ResponseEntity.ok(dashboardService.getCompletionRatesByDepartment());
    }

}
