package returnstrackingsystem.dtos.response;

import lombok.Data;
import returnstrackingsystem.domain.Submission;

import java.time.OffsetDateTime;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 14:48
 * projectName compliance-returns-tracker
 **/

@Data
public class TaskTrackResponse {
    private Long id;
    private Submission submission;
    private String userEmail;
    private String msTaskId;
    private String msEventId;
    private OffsetDateTime lastNotificationAt;
    private OffsetDateTime completedAt;
    private boolean completed;
}
