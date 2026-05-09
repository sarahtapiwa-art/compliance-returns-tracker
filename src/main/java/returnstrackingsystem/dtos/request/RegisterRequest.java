package returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import returnstrackingsystem.domain.enums.Role;
import returnstrackingsystem.customvalidation.Email;

import java.util.Set;



@Schema(description = "Request object for registering a new user")
public record RegisterRequest(
        @Schema(description = "Username for the new user", example = "john_doe")
        String username,

        @Schema(description = "Email address of the user", example = "john@gmail.com")
        @Email
        String email,

        @Schema(description = "Roles assigned to the user", example = "[\"USER\"]")
        Set<Role> roles
) {}


