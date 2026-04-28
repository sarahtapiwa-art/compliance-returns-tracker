package zw.co.nbs.returnstrackingsystem.dtos.response;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 28/8/2025
 * createdTime 12:12
 * projectName compliance-returns-tracker
 **/

@Builder
@Schema(description = "Generic paginated response wrapper")
public record PagedResponse<T>(
        @Schema(description = "List of items for the current page")
        List<T> content,

        @Schema(description = "Current page number (zero-based)", example = "0")
        int pageNumber,

        @Schema(description = "Number of items per page", example = "20")
        int pageSize,

        @Schema(description = "Total number of elements across all pages", example = "100")
        long totalElements,

        @Schema(description = "Total number of pages", example = "5")
        int totalPages,

        @Schema(description = "Indicator if this is the last page", example = "false")
        boolean last
) {}


