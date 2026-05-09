package returnstrackingsystem.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:49
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendPreparationResponse {
    private String credentialToken;
    private Long expiresIn;
    private OffsetDateTime expiresAt;
}
