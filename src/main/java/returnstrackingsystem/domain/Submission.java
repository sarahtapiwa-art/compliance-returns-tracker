package returnstrackingsystem.domain;

import jakarta.persistence.*;
import lombok.*;
import returnstrackingsystem.domain.enums.SubmissionStatus;

import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(columnList = "dueAt"),
        @Index(columnList = "status")
})
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @ManyToOne
    private ReturnDefinition returnDefinition;
    private OffsetDateTime periodStart;
    @Builder.Default
    private boolean deleted = false;
    private OffsetDateTime periodEnd;
    private OffsetDateTime dueAt;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;
    public String periodLabel() {
        if (periodStart == null || periodEnd == null) return "N/A";
        return periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
    }
}