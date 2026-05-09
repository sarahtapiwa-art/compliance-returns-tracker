package returnstrackingsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import returnstrackingsystem.domain.enums.NotificationType;

import java.time.OffsetDateTime;

/**
 * createdBy       lorraine.mhizha
 * createdDate     22/8/2025
 * createdTime     11:32
 * projectName     compliance-returns-tracker
 **/
@Entity
@Table(indexes = @Index(columnList = "sentAt"))
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Submission submission;
    private NotificationType notificationType;
    private String emailTarget;
    private String emailSubject;
    private OffsetDateTime sentAt;
    private String result;
}
