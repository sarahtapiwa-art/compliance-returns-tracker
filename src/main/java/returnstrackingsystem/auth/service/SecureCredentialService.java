package returnstrackingsystem.auth.service;

import returnstrackingsystem.util.TemporaryCredentials;

import java.security.Principal;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:26
 * projectName compliance-returns-tracker
 **/

public interface SecureCredentialService {
    String storeTemporaryCredentials(String username, String password, Principal user);
    TemporaryCredentials getAndRemoveCredentials(String token);
    void forceExpireToken(String token);
}
