package zw.co.nbs.returnstrackingsystem.service.impl;

import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.TodoTask;
import com.microsoft.graph.requests.GraphServiceClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zw.co.nbs.returnstrackingsystem.domain.NotificationLog;
import zw.co.nbs.returnstrackingsystem.domain.ScheduleRule;
import zw.co.nbs.returnstrackingsystem.domain.Submission;
import zw.co.nbs.returnstrackingsystem.domain.enums.NotificationType;
import zw.co.nbs.returnstrackingsystem.domain.enums.SubmissionStatus;
import zw.co.nbs.returnstrackingsystem.repository.NotificationLogRepository;
import zw.co.nbs.returnstrackingsystem.repository.ScheduleRuleRepository;
import zw.co.nbs.returnstrackingsystem.repository.SubmissionRepository;
import zw.co.nbs.returnstrackingsystem.repository.TaskTrackRepository;
import zw.co.nbs.returnstrackingsystem.service.EmailService;
import zw.co.nbs.returnstrackingsystem.service.NotificationService;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

/**
 * createdBy romeo
 * createdDate 2/9/2025
 * createdTime 11:04
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

        private final SubmissionRepository submissionRepository;
        private final ScheduleRuleRepository scheduleRuleRepository;
        private final TaskTrackRepository trackRepository;
        private final NotificationLogRepository notificationLogRepository;
        private final GraphServiceClient<?> graphClient;
        private final EmailService emailService;

        @Value("${spring.mail.from}")
        private String mailFrom;

        @Override
        @Transactional
        public void processReminders() {
                OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Africa/Harare"));

                List<Submission> all = new ArrayList<>();
                all.addAll(submissionRepository.findByStatus(SubmissionStatus.UPLOADED));
                all.addAll(submissionRepository.findByStatus(SubmissionStatus.PENDING));

                for (Submission submission : all) {
                        ScheduleRule rule = scheduleRuleRepository
                                        .findByReturnDefinition(submission.getReturnDefinition())
                                        .orElse(null);

                        List<Integer> days = (rule != null && rule.getRemindDaysBefore() != null)
                                        ? parseDays(valueOf(rule.getRemindDaysBefore()))
                                        : List.of(7, 3, 1);

                        for (int d : days) {
                                OffsetDateTime targetMoment = submission.getDueAt()
                                                .atZoneSameInstant(ZoneId.of("Africa/Harare"))
                                                .minusDays(d)
                                                .withMinute(0)
                                                .withSecond(0)
                                                .withNano(0)
                                                .toOffsetDateTime();

                                if (withinWindow(now, targetMoment)) {
                                        trackRepository.findBySubmission(submission).ifPresent(track -> {
                                                try {
                                                        TodoTask patch = new TodoTask();
                                                        patch.reminderDateTime = harare(targetMoment.plusHours(1));

                                                        graphClient.users(track.getUserEmail())
                                                                        .todo().lists("Tasks")
                                                                        .tasks(track.getMsTaskId())
                                                                        .buildRequest().patch(patch);
                                                } catch (Exception ex) {
                                                        log.warn("Failed to bump reminder for submission#{}: {}",
                                                                        submission.getId(), ex.getMessage());
                                                }
                                        });

                                        try {
                                                NotificationLog logEntry = NotificationLog.builder()
                                                                .submission(submission)
                                                                .notificationType(NotificationType.REMINDER)
                                                                .emailTarget(
                                                                                submission.getReturnDefinition()
                                                                                        .getResponsiblePerson()
                                                                                                .getEmail())
                                                                .emailSubject("Reminder: " + submission
                                                                                .getReturnDefinition().getTitle())
                                                                .sentAt(OffsetDateTime.now())
                                                                .result("OK")
                                                                .build();

                                                notificationLogRepository.save(logEntry);
                                                notificationLogRepository.flush();
                                                log.info("NotificationLog saved for submission ID {}",
                                                                submission.getId());

                                        } catch (Exception e) {
                                                log.error("Failed to save NotificationLog for submission#{}: {}",
                                                                submission.getId(), e.getMessage(), e);
                                        }
                                }
                        }
                }
        }

        @Override
        public void escalateOverdue() {
                ZoneId harareZone = ZoneId.of("Africa/Harare");
                OffsetDateTime now = OffsetDateTime.now(harareZone);
                List<Submission> overdue = submissionRepository.findOverdueNotSubmitted();

                for (Submission submission : overdue) {
                        try {
                                OffsetDateTime dueAt = submission.getDueAt().atZoneSameInstant(harareZone)
                                                .toOffsetDateTime();

                                var lastEscalationOpt = notificationLogRepository
                                                .findTopBySubmissionAndNotificationTypeOrderBySentAtDesc(
                                                                submission,
                                                                NotificationType.ESCALATION);

                                boolean shouldEscalate = false;

                                if (lastEscalationOpt.isEmpty()) {
                                        // First escalation: 10 minutes after due time
                                        if (now.isAfter(dueAt.plusMinutes(10))) {
                                                shouldEscalate = true;
                                                log.debug("First escalation triggered for submission ID: {} (10 mins past due)",
                                                                submission.getId());
                                        }
                                } else {
                                        // Subsequent escalations: As configured in ScheduleRule
                                        ScheduleRule rule = scheduleRuleRepository.findByReturnDefinition(
                                                        submission.getReturnDefinition()).orElse(null);

                                        if (rule != null && rule.getEscalateAfterHours() != null) {
                                                OffsetDateTime lastSent = lastEscalationOpt.get().getSentAt()
                                                                .atZoneSameInstant(harareZone).toOffsetDateTime();
                                                OffsetDateTime nextEscalationThreshold = lastSent
                                                                .plusHours(rule.getEscalateAfterHours());

                                                if (now.isAfter(nextEscalationThreshold)) {
                                                        shouldEscalate = true;
                                                        log.debug("Subsequent escalation triggered for submission ID: {} ({} hours since last)",
                                                                        submission.getId(),
                                                                        rule.getEscalateAfterHours());
                                                }
                                        }
                                }

                                if (shouldEscalate) {
                                        String regulatoryReportTitle = submission.getReturnDefinition().getTitle();
                                        String body = emailService.buildOverdueReportEmail(regulatoryReportTitle,
                                                        submission.getDueAt().atZoneSameInstant(harareZone).toString(),
                                                        submission.getStatus().toString());

                                        String targetEmail = submission.getReturnDefinition().getDepartment()
                                                        .getEscalationEmail();

                                        if (targetEmail == null || targetEmail.isBlank()) {
                                                log.warn("No escalation email configured for department: {} (Submission ID: {}), skipping email",
                                                                submission.getReturnDefinition().getDepartment()
                                                                                .getDepartmentName(),
                                                                submission.getId());
                                                continue;
                                        }

                                        String subject = submission.getStatus().name() + ": " + regulatoryReportTitle;
                                        emailService.send(mailFrom,
                                                        targetEmail,
                                                        null,
                                                        subject,
                                                        body);

                                        saveEscalationLog(submission, regulatoryReportTitle, targetEmail);

                                        log.info("Escalation email sent to {} for overdue submission ID: {} ({})",
                                                        targetEmail, submission.getId(), regulatoryReportTitle);
                                }
                        } catch (Exception e) {
                                log.error("Critical error escalating overdue submission ID: {}. Skipping record to avoid blocking job.",
                                                submission.getId(), e);
                        }
                }
        }

        @Transactional
        protected void saveEscalationLog(Submission submission, String title, String targetEmail) {
                notificationLogRepository.save(NotificationLog.builder()
                                .submission(submission)
                                .notificationType(NotificationType.ESCALATION)
                                .emailTarget(targetEmail)
                                .emailSubject("Overdue: " + title)
                                .sentAt(OffsetDateTime.now(ZoneId.of("Africa/Harare")))
                                .result("OK")
                                .build());
        }

        private boolean withinWindow(OffsetDateTime now, OffsetDateTime targetMoment) {
                return !now.isBefore(targetMoment.minusMinutes(15))
                                && !now.isAfter(targetMoment.plusMinutes(15));
        }

        private static DateTimeTimeZone harare(OffsetDateTime offsetDateTime) {
                var d = new DateTimeTimeZone();
                d.timeZone = "Africa/Harare";
                d.dateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("Africa/Harare"))
                                .toLocalDateTime().toString();

                return d;
        }

        private List<Integer> parseDays(String daysStr) {
                return Arrays.stream(daysStr.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
        }
}
