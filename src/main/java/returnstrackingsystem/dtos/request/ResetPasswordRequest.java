package returnstrackingsystem.dtos.request;

/**
 * createdBy romeo
 * createdDate 18/11/2025
 * createdTime 16:29
 * projectName compliance-returns-tracker
 **/

public record ResetPasswordRequest(
        String token,
        String newPassword,
        String confirmNewPassword
) {}
