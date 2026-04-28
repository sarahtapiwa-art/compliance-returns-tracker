package zw.co.nbs.returnstrackingsystem.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zw.co.nbs.returnstrackingsystem.domain.ResponsiblePerson;
import zw.co.nbs.returnstrackingsystem.domain.enums.Frequency;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a regulatory report definition")
public class ReturnDefinitionRequest {

    @NotBlank(message = "Report title is mandatory")
    @Size(max = 255, message = "Report title must not exceed 255 characters")
    @Schema(description = "Title of the regulatory report", example = "Quarterly Financial Report")
    private String title;

    @NotBlank(message = "Regulatory body is mandatory")
    @Schema(description = "Name of the regulatory body", example = "Reserve Bank of Zimbabwe")
    private String regulatoryBody;

    @NotBlank(message = "Regulatory email is mandatory")
    @Schema(description = "Email address of the regulatory body", example = "regulator@nbs.co.zw")
    private String regulatoryEmail;

    @NotNull(message = "Report frequency must not be null")
    @Schema(description = "Frequency of report submission", example = "QUARTERLY")
    private Frequency frequency;

    @NotNull(message = "Submission deadline is mandatory")
    @Schema(description = "Deadline for report submission", example = "2025-09-30T23:59")
    private LocalDateTime submissionDeadline;

    @NotNull(message = "Responsible department id is mandatory")
    @Schema(description = "ID of the department responsible for the report", example = "1")
    private Long responsibleDepartmentId;

    @Schema(description = "Optional description of the report", example = "This report covers the quarterly financial activities.")
    private String description;

    @NotNull
    @Schema(description = "Responsible person to send the report")
    private ResponsiblePerson responsiblePerson;
}