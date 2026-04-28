package zw.co.nbs.returnstrackingsystem.dtos.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import zw.co.nbs.returnstrackingsystem.customvalidation.NbsEmail;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a department")
public class DepartmentRequest {

    @NotBlank(message = "Department name is mandatory")
    @Schema(description = "Name of the department", example = "Finance")
    private String departmentName;

    @Email(message = "Email should be valid")
    @Schema(description = "Email address of the head of department", example = "contact@nbs.co.zw")
    @NbsEmail
    private String escalationEmail;

    @Email(message = "Email should be valid")
    @Schema(description = "Email address of the head of department", example = "john.doe@nbs.co.zw")
    @NbsEmail
    private String headOfDepartmentEmail;

}
