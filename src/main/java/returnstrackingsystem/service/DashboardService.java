package returnstrackingsystem.service;

import returnstrackingsystem.dtos.request.ReportFilterRequest;
import returnstrackingsystem.dtos.response.CompletionRateResponse;
import returnstrackingsystem.dtos.response.DashboardOverviewResponse;
import returnstrackingsystem.dtos.response.SubmissionDashboardResponse;

import java.security.Principal;
import java.util.List;

public interface DashboardService {
    DashboardOverviewResponse getOverview(Principal currentUser);
    List<SubmissionDashboardResponse> getUpcomingSubmissions(int days);
    List<SubmissionDashboardResponse> getOverdueSubmissions(Principal currentUser);
    List<CompletionRateResponse> getCompletionRatesByDepartment();
    List<SubmissionDashboardResponse> generateCustomReport(ReportFilterRequest filter);
}
