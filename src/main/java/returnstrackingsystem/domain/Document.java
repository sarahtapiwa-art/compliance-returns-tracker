package returnstrackingsystem.domain;


import jakarta.persistence.*;
import lombok.*;
import returnstrackingsystem.domain.enums.DocumentStatus;

import java.time.OffsetDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Submission submission;
    private String fileName;
    private String storageUrl;
    private String fileType;
    private String contentType;
    private String reason;
    @Builder.Default
    private boolean isArchived = false;
    @Enumerated(STRING)
    private DocumentStatus status;
    private String uploadedBy;
    private OffsetDateTime uploadedAt;

}