package returnstrackingsystem.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import returnstrackingsystem.domain.enums.Frequency;

import java.time.OffsetDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String regulatoryBody;
    private Long documentId;
    @Email
    private String regulatoryEmail;
    @Enumerated(STRING)
    private Frequency frequency;
    private OffsetDateTime submissionDeadline;
    @ManyToOne(optional = false)
    private Department department;
    private String description;
    @Builder.Default
    private boolean syncCalendar = false;
    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private boolean deleted = false;

    @Embedded
    ResponsiblePerson responsiblePerson;
}