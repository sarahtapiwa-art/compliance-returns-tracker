package returnstrackingsystem.auth.service;

import returnstrackingsystem.domain.RefreshToken;
import returnstrackingsystem.domain.User;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 13:55
 * projectName compliance-returns-tracker
 **/

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    User validateRefreshToken(String token);
    void deleteRefreshToken(User user);
}
