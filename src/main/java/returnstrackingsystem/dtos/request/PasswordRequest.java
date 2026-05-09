package returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 10:20
 * projectName compliance-returns-tracker
 **/

@Schema(description = "Request object for changing a user's password")
public record PasswordRequest(

        @Schema(description = "Current password of the user", example = "P@ssw0rd!", format = "password")
        String oldPassword,

        @Schema(description = "New password to be set", example = "N3wP@ssw0rd!", format = "password")
        String newPassword,

        @Schema(description = "Confirm new password", example = "N3wP@ssw0rd!", format = "password")
        String confirmNewPassword
) {}

