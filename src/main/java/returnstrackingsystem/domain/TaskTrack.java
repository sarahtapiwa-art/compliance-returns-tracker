package returnstrackingsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTrack {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @OneToOne(optional = false)
 private Submission submission;
 private String userEmail;
 private String msTaskId;
 private String msEventId;
 private OffsetDateTime lastNotificationAt;
 private OffsetDateTime completedAt;
 private boolean completed;
}
