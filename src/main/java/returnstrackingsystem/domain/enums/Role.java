package returnstrackingsystem.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 10:06
 * projectName compliance-returns-tracker
 **/

@Schema(description = "User role in the system", example = "USER")
public enum Role {
    @Schema(description = "Super system administrator with full access")
    SUPER_SYSTEM_ADMIN,

    @Schema(description = "Administrator with elevated privileges")
    ADMIN,

    @Schema(description = "Standard user with limited access")
    USER
}
