package returnstrackingsystem.dtos.request;



public record ResetPasswordRequest(
        String token,
        String newPassword,
        String confirmNewPassword
) {}
