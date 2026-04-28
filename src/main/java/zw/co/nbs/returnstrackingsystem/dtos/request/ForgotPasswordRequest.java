package zw.co.nbs.returnstrackingsystem.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 18/11/2025
 * createdTime 15:18
 * projectName compliance-returns-tracker
 **/

@Builder
@Schema(description = "Request object for forgot password")
public record ForgotPasswordRequest(
        @Schema(description = "Email address of the user", example = "jon.doe@nbs.co.zw")
        @Email String email
) {}
