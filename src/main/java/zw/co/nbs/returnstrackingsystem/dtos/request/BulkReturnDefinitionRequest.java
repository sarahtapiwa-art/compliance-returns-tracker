package zw.co.nbs.returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 17/10/2025
 * createdTime 11:01
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for bulk upload of regulatory report definitions")
public class BulkReturnDefinitionRequest {

    @NotNull(message = "Reports list cannot be null")
    @Size(min = 1, message = "At least one report must be provided")
    @Schema(description = "List of report definitions to upload")
    private List<ReturnDefinitionRequest> reports;
}
