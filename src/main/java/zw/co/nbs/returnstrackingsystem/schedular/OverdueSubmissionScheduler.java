package zw.co.nbs.returnstrackingsystem.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zw.co.nbs.returnstrackingsystem.service.NotificationService;
import zw.co.nbs.returnstrackingsystem.service.SubmissionService;

/**
 * createdBy romeo
 * createdDate 6/11/2025
 * createdTime 12:00
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueSubmissionScheduler {

    private final SubmissionService submissionService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * *")
    public void checkForOverdueSubmissions() {
        log.debug("Checking for overdue submissions and escalations...");
        try {
            submissionService.flagOverdueSubmissions();
            notificationService.escalateOverdue();
        } catch (Exception e) {
            log.error("Error while checking for overdue submissions/escalations", e);
        }
    }
}
