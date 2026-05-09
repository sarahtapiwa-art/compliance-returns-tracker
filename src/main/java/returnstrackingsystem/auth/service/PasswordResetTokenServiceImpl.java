package returnstrackingsystem.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import returnstrackingsystem.auth.jwt.JwtUtil;
import returnstrackingsystem.domain.PasswordResetToken;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.repository.PasswordResetTokenRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * createdBy romeo
 * createdDate 18/11/2025
 * createdTime 15:24
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService{

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${token.reset.expiration}")
    private int tokenResetExpiration;


    @Override
    public String generatePasswordResetToken(User user) {
        log.info("Generating password reset token for user {}", user.getUsername());

        invalidateExistingTokens(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(calculateExpiryDate())
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated successfully for user: {}", user.getUsername());
        return token;
    }

    @Override
    public PasswordResetToken validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RecordNotFoundException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("Password reset token has already been used");
        }

        return resetToken;
    }

    @Override
    public void deleteToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, tokenResetExpiration);
        return new Date(cal.getTime().getTime());
    }

    private void invalidateExistingTokens(User user) {
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository
                .findByUserAndUsedFalseAndExpiryDateAfter(user, new Date());

        for (PasswordResetToken activeToken : activeTokens) {
            activeToken.setUsed(true);
            log.debug("Invalidated existing token for user: {}", user.getUsername());
        }

        if (!activeTokens.isEmpty()) {
            passwordResetTokenRepository.saveAll(activeTokens);
            log.info("Invalidated {} existing active tokens for user: {}", activeTokens.size(), user.getUsername());
        }
    }
}
