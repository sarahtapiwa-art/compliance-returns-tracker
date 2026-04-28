package zw.co.nbs.returnstrackingsystem.dtos.response;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 09:00
 * projectName compliance-returns-tracker
 **/

public record AuthResponse(
        String token,
        String refreshToken
){}

