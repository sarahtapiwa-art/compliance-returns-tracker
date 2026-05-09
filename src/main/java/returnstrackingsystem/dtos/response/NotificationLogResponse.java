package returnstrackingsystem.dtos.response;

import lombok.Data;
import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.domain.enums.NotificationType;

import java.time.OffsetDateTime;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 13:52
 * projectName compliance-returns-tracker
 **/

@Data
public class NotificationLogResponse {
    private Long id;
    private Submission submission;
    private NotificationType notificationType;
    private String emailTarget;
    private String emailSubject;
    private OffsetDateTime sentAt;
    private String result;

}
