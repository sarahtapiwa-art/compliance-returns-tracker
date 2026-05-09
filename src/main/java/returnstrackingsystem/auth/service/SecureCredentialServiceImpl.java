package returnstrackingsystem.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.exception.InvalidCredentialTokenException;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.repository.UserRepository;
import returnstrackingsystem.util.TemporaryCredentials;

import java.security.Principal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:28
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class SecureCredentialServiceImpl implements SecureCredentialService{

    private final Map<String, TemporaryCredentials> tempCredentialStore = new ConcurrentHashMap<>();
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    @Override
    public String storeTemporaryCredentials(String username, String password, Principal currentUser) {
        log.info("Storing temporary credentials for user {}", username);

        User user = userRepository.findByUsername(currentUser.getName())
                .orElseThrow(() -> new RecordNotFoundException("User not found"));
        String token = generateSecureToken();

        TemporaryCredentials tempCreds = TemporaryCredentials.builder()
                .encryptedUsername(encryptionService.encrypt(username))
                .encryptedPassword(encryptionService.encrypt(password))
                .userId(user.getId())
                .createdAt(OffsetDateTime.now())
                .used(false)
                .build();

        tempCredentialStore.put(token, tempCreds);

        // Schedule cleanup after 5 minutes
        scheduleCleanup(token, Duration.ofMinutes(5));
        return token;
    }

    @Override
    public TemporaryCredentials getAndRemoveCredentials(String token) {
        TemporaryCredentials creds = tempCredentialStore.get(token);
        if (creds == null) {
            throw new InvalidCredentialTokenException("Credential token expired or invalid");
        }
        if (creds.isUsed()) {
            throw new InvalidCredentialTokenException("Credential token already used");
        }

        // Remove from store immediately
        tempCredentialStore.remove(token);
        creds.setUsed(true);
        return creds;
    }

    @Override
    public void forceExpireToken(String token) {
        tempCredentialStore.remove(token);
    }
    private String generateSecureToken() {
        return UUID.randomUUID().toString();
    }

    private void scheduleCleanup(String token, Duration duration) {
        CompletableFuture.delayedExecutor(duration.toSeconds(), TimeUnit.SECONDS)
                .execute(() -> tempCredentialStore.remove(token));
    }
}
