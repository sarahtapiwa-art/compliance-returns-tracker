package zw.co.nbs.returnstrackingsystem.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zw.co.nbs.returnstrackingsystem.domain.RefreshToken;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.repository.RefreshTokenRepository;
import zw.co.nbs.returnstrackingsystem.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 13:57
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private int refreshTokenDurationMinutes;

    @Transactional
    @Override
    public RefreshToken createRefreshToken(User user) {
        return refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setToken(UUID.randomUUID().toString());
                    existingToken.setExpiryDate(LocalDateTime.now().plusMinutes(refreshTokenDurationMinutes));
                    return refreshTokenRepository.save(existingToken);
                })
                .orElseGet(() -> {
                    RefreshToken newToken = new RefreshToken();
                    newToken.setUser(user);
                    newToken.setToken(UUID.randomUUID().toString());
                    newToken.setExpiryDate(LocalDateTime.now().plusMinutes(refreshTokenDurationMinutes));
                    return refreshTokenRepository.save(newToken);
                });
    }


    @Override
    public User validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken.getUser();
    }

    @Override
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
