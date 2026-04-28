package zw.co.nbs.returnstrackingsystem.dtos.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a document")
public class DocumentRequest {

    @NotBlank(message = "Document type is mandatory")
    @Schema(description = "Type of the document", example = "Financial Report")
    private String documentType;

    @NotBlank(message = "File path is mandatory")
    @Schema(description = "Path to the file or storage location", example = "/uploads/reports/report1.pdf")
    private String filePath;
}
