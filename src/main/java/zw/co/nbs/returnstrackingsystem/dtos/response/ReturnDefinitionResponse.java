package zw.co.nbs.returnstrackingsystem.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zw.co.nbs.returnstrackingsystem.domain.Department;
import zw.co.nbs.returnstrackingsystem.domain.ResponsiblePerson;
import zw.co.nbs.returnstrackingsystem.domain.enums.Frequency;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for return definitions")
public class ReturnDefinitionResponse {

    @Schema(description = "Unique identifier of the return definition", example = "1")
    private Long id;

    @Schema(description = "Title of the report", example = "Monthly Financial Report")
    private String title;

    @Schema(description = "Regulatory body responsible for this report", example = "Reserve Bank")
    private String regulatoryBody;

    @Schema(description = "Unique identifier of the document", example = "1")
    private Long documentId;

    @Schema(description = "Email of the regulatory body", example = "regulator@nbs.co.zw")
    private String regulatoryEmail;

    @Schema(description = "Frequency of the report submission")
    private Frequency frequency;

    @Schema(description = "Deadline for report submission", example = "2025-09-30T23:59:59Z")
    private OffsetDateTime submissionDeadline;

    @Schema(description = "Details of the responsible department")
    private Department department;

    @Schema(description = "Details of the responsible person")
    private ResponsiblePerson responsiblePerson;

    @Schema(description = "Optional description of the report")
    private String description;

    @Schema(description = "Indicates if the report is synced with calendar", defaultValue = "false")
    private boolean syncCalendar;

    @Schema(description = "Indicates if the report is active", defaultValue = "true")
    private boolean active;

    @Schema(description = "Indicates if the report is deleted", defaultValue = "false")
    private boolean deleted;
}
