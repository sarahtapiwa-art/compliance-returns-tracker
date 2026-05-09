package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.dtos.request.ReportFilterRequest;
import returnstrackingsystem.dtos.response.CompletionRateResponse;
import returnstrackingsystem.dtos.response.DashboardOverviewResponse;
import returnstrackingsystem.dtos.response.SubmissionDashboardResponse;
import returnstrackingsystem.dtos.response.*;

import returnstrackingsystem.domain.enums.SubmissionStatus;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.mapper.SubmissionDashboardMapper;
import returnstrackingsystem.repository.SubmissionRepository;
import returnstrackingsystem.repository.UserRepository;
import returnstrackingsystem.service.DashboardService;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

        private final SubmissionRepository submissionRepository;
        private final UserRepository userRepository;

        @Value("${superadmin.role}")
        private String superAdminUserRole;

        @Override
        public DashboardOverviewResponse getOverview(Principal currentUser) {

                log.info("Getting dashboard overview");
                User user = userRepository.findByUsername(currentUser.getName())
                                .orElseThrow(() -> new RecordNotFoundException("User not found"));

                boolean isSuperAdmin = user.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority()
                                                .equals(superAdminUserRole));

                Long departmentId = isSuperAdmin ? null : user.getDepartment().getId();

                long total = submissionRepository.countByDepartment(departmentId);
                long completed = submissionRepository.countByStatusAndDepartment(SubmissionStatus.SUBMITTED,
                                departmentId);
                long overdue = submissionRepository.findByStatusInAndDepartment(
                                Arrays.asList(SubmissionStatus.OVERDUE, SubmissionStatus.UPLOADED_OVERDUE),
                                departmentId)
                                .size();

                List<SubmissionStatus> upcomingStatuses = Arrays.asList(
                                SubmissionStatus.PENDING,
                                SubmissionStatus.UPLOADED);

                long upcoming = submissionRepository.findUpcomingByDepartment(
                                OffsetDateTime.now(),
                                OffsetDateTime.now().plusDays(7),
                                upcomingStatuses,
                                departmentId).size();

                double completionRate = total == 0 ? 0 : (completed * 100.0 / total);

                return DashboardOverviewResponse.builder()
                                .totalSubmissions(total)
                                .completedSubmissions(completed)
                                .overdueSubmissions(overdue)
                                .upcomingSubmissions(upcoming)
                                .completionRate(completionRate)
                                .build();
        }

        @Override
        public List<SubmissionDashboardResponse> getUpcomingSubmissions(int days) {
                List<SubmissionStatus> excluded = Arrays.asList(SubmissionStatus.CLOSED, SubmissionStatus.SUBMITTED);
                return submissionRepository.findUpcoming(
                                OffsetDateTime.now(),
                                OffsetDateTime.now().plusDays(days),
                                excluded)
                                .stream().map(SubmissionDashboardMapper::toResponse).toList();
        }

        @Override
        public List<SubmissionDashboardResponse> getOverdueSubmissions(Principal currentUser) {
                log.info("Getting overdue submissions");

                User user = userRepository.findByUsername(currentUser.getName())
                                .orElseThrow(() -> new RecordNotFoundException("User not found"));

                boolean isSuperAdmin = user.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority()
                                                .equals(superAdminUserRole));

                Long departmentId = isSuperAdmin ? null : user.getDepartment().getId();

                List<SubmissionStatus> overdueStatuses = Arrays.asList(
                                SubmissionStatus.OVERDUE,
                                SubmissionStatus.UPLOADED_OVERDUE);

                return submissionRepository.findByStatusInAndDepartment(overdueStatuses, departmentId)
                                .stream()
                                .map(SubmissionDashboardMapper::toResponse)
                                .toList();
        }

        @Override
        public List<CompletionRateResponse> getCompletionRatesByDepartment() {
                return submissionRepository.findCompletionRatesByDepartment().stream()
                                .map(obj -> new CompletionRateResponse((String) obj[0], (Double) obj[1]))
                                .toList();
        }

        @Override
        public List<SubmissionDashboardResponse> generateCustomReport(ReportFilterRequest filter) {
                return submissionRepository.findAll().stream()
                                .filter(s -> filter.getDepartment() == null ||
                                                s.getReturnDefinition().getDepartment().getDepartmentName()
                                                                .equalsIgnoreCase(filter.getDepartment()))
                                .filter(s -> filter.getStatus() == null ||
                                                s.getStatus().name().equalsIgnoreCase(filter.getStatus()))
                                .filter(s -> filter.getFromDate() == null
                                                || !s.getDueAt().isBefore(filter.getFromDate()))
                                .filter(s -> filter.getToDate() == null || !s.getDueAt().isAfter(filter.getToDate()))
                                .map(SubmissionDashboardMapper::toResponse)
                                .toList();
        }
}
