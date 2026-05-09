package returnstrackingsystem.dtos.response;

import returnstrackingsystem.domain.Department;
import returnstrackingsystem.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * createdBy romeo
 * createdDate 28/8/2025
 * createdTime 12:00
 * projectName compliance-returns-tracker
 **/

@Schema(description = "Response object representing a user")
public record UserResponse(
        @Schema(description = "Unique identifier of the user", example = "123")
        Long id,

        @Schema(description = "Username of the user", example = "johndoe")
        String username,

        @Schema(description = "Email address of the user", example = "johndoe@example.com")
        String email,

        @Schema(description = "Department of the user", example = "Finance")
        Department department,

        @Schema(description = "Roles assigned to the user")
        Set<Role> roles
) {}

