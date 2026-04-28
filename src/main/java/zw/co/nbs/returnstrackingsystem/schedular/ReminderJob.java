package zw.co.nbs.returnstrackingsystem.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import zw.co.nbs.returnstrackingsystem.domain.*;

import zw.co.nbs.returnstrackingsystem.service.impl.NotificationServiceImpl;

import java.time.*;

/**
 * createdBy romeo
 * createdDate 20/10/2025
 * createdTime 08:26
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReminderJob {

    private final NotificationServiceImpl notificationService;

    @Scheduled(cron = "0 */15 * * * *", zone = "Africa/Harare")
    public void cadenceReminders() {
        notificationService.processReminders();
    }
}
