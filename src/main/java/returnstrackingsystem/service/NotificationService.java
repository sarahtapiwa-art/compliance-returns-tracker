package returnstrackingsystem.service;

/**
 * createdBy romeo
 * createdDate 2/9/2025
 * createdTime 11:31
 * projectName compliance-returns-tracker
 **/

public interface NotificationService {
    void processReminders();
    void escalateOverdue();
}
