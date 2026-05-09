package returnstrackingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import returnstrackingsystem.domain.PasswordResetToken;
import returnstrackingsystem.domain.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * createdBy romeo
 * createdDate 18/11/2025
 * createdTime 15:23
 * projectName compliance-returns-tracker
 **/

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    void deleteByUser(User user);

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, Date date);
}
