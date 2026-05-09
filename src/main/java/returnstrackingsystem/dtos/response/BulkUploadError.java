package returnstrackingsystem.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * createdBy romeo
 * createdDate 17/10/2025
 * createdTime 10:59
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error details for failed bulk upload items")
public class BulkUploadError {

    @Schema(description = "Index of the failed report in the input list")
    private int index;

    @Schema(description = "Report title (if available)")
    private String title;

    @Schema(description = "Error message")
    private String error;

    @Schema(description = "Field that caused the error")
    private String field;
}
