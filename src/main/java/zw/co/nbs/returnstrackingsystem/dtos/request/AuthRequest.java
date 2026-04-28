package zw.co.nbs.returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 08:59
 * projectName compliance-returns-tracker
 **/

@Schema(description = "Authentication request containing user credentials")
public record AuthRequest(
        @Schema(description = "Username of the user", example = "johndoe")
        String username,

        @Schema(description = "Password of the user", example = "P@ssw0rd!")
        String password
) {}


