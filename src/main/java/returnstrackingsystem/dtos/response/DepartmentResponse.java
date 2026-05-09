package returnstrackingsystem.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing a department")
public class DepartmentResponse {

    @Schema(description = "Unique identifier of the department", example = "1")
    private Long id;

    @Schema(description = "Name of the department", example = "Finance")
    private String departmentName;

    @Schema(description = "Group email address of the department", example = "operations@nbs.co.zw")
    private String escalationEmail;

    @Schema(description = "Email address of the head of department", example = "john.doe@nbs.co.zw")
    private String headOfDepartmentEmail;

    @Schema(description = "Flag to indicate if the department is deleted", example = "false")
    private boolean deleted;
}

