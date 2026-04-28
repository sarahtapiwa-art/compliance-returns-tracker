package zw.co.nbs.returnstrackingsystem.service;

import zw.co.nbs.returnstrackingsystem.dtos.request.ReportFilterRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.*;

import java.security.Principal;
import java.util.List;

public interface DashboardService {
    DashboardOverviewResponse getOverview(Principal currentUser);
    List<SubmissionDashboardResponse> getUpcomingSubmissions(int days);
    List<SubmissionDashboardResponse> getOverdueSubmissions(Principal currentUser);
    List<CompletionRateResponse> getCompletionRatesByDepartment();
    List<SubmissionDashboardResponse> generateCustomReport(ReportFilterRequest filter);
}
