package zw.co.nbs.returnstrackingsystem.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 17/10/2025
 * createdTime 11:00
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for bulk upload operation")
public class BulkUploadResponse {

    @Schema(description = "Total number of items processed")
    private int totalProcessed;

    @Schema(description = "Number of successfully processed items")
    private int successfulCount;

    @Schema(description = "Number of failed items")
    private int failedCount;

    @Schema(description = "List of errors for failed items")
    private List<BulkUploadError> errors;

    @Schema(description = "List of successfully processed item IDs")
    private List<Long> successfulIds;
}
