package zw.co.nbs.returnstrackingsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.nbs.returnstrackingsystem.domain.NotificationLog;
import zw.co.nbs.returnstrackingsystem.domain.Submission;
import zw.co.nbs.returnstrackingsystem.domain.enums.NotificationType;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findAllBySubmission_ReturnDefinition_Department_DepartmentName(String departmentName,
            Pageable pageable);

    /**
     * Check if a notification of a specific type has already been sent for a
     * submission
     * 
     * @param submission       The submission to check
     * @param notificationType The type of notification (e.g., ESCALATION)
     * @return true if notification already sent, false otherwise
     */
    boolean existsBySubmissionAndNotificationType(Submission submission, NotificationType notificationType);

    java.util.Optional<NotificationLog> findTopBySubmissionAndNotificationTypeOrderBySentAtDesc(Submission submission,
            NotificationType notificationType);
}
