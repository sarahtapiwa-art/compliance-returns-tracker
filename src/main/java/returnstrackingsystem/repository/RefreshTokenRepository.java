package returnstrackingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import returnstrackingsystem.domain.RefreshToken;
import returnstrackingsystem.domain.User;

import java.util.Optional;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 13:48
 * projectName compliance-returns-tracker
 **/

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    Optional<RefreshToken> findByUser(User user);
}
