package returnstrackingsystem.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import returnstrackingsystem.domain.*;

import returnstrackingsystem.service.impl.NotificationServiceImpl;


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
