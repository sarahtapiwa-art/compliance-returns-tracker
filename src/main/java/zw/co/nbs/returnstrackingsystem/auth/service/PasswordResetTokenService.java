package zw.co.nbs.returnstrackingsystem.auth.service;

import zw.co.nbs.returnstrackingsystem.domain.PasswordResetToken;
import zw.co.nbs.returnstrackingsystem.domain.User;

/**
 * createdBy romeo
 * createdDate 18/11/2025
 * createdTime 15:22
 * projectName compliance-returns-tracker
 **/

public interface PasswordResetTokenService {
    String generatePasswordResetToken(User user);
    PasswordResetToken validatePasswordResetToken(String token);
    void deleteToken(PasswordResetToken token);
}
