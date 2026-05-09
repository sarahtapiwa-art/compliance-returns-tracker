package returnstrackingsystem.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import returnstrackingsystem.domain.Submission;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import returnstrackingsystem.domain.enums.DocumentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing a document")
public class DocumentResponse {

    @Schema(description = "Unique identifier of the document", example = "1")
    private Long id;

    @Schema(description = "The submission this document is attached to")
    private Submission submission;

    @Schema(description = "Type of the document", example = "Financial Report")
    private String contentType;

    @Schema(description = "File Type")
    private String fileType;

    @Schema(description = "Reason in case its rejected", example = "You seem to have uploaded" +
            " the wrong report")
    private String reason;

    @Schema(description = "Path or URL where the document is stored", example = "/files/reports/report1.pdf")
    private String storageUrl;

    @Schema(description = "Date the document was uploaded", example = "2025-08-25T14:30:00Z")
    private OffsetDateTime uploadedAt;

    @Schema(description = "Name of the person who uploaded the document", example = "")
    private String uploadedBy;

    @Schema(description = "Document status", example = "VERIFIED")
    private DocumentStatus status;
}

