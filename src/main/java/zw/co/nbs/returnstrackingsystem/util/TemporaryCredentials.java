package zw.co.nbs.returnstrackingsystem.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:25
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemporaryCredentials {
    private String encryptedUsername;
    private String encryptedPassword;
    private Long userId;
    private OffsetDateTime createdAt;
    private boolean used;
}
