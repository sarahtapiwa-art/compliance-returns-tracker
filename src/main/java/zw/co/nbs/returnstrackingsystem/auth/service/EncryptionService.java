package zw.co.nbs.returnstrackingsystem.auth.service;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:35
 * projectName compliance-returns-tracker
 **/

public interface EncryptionService {
    String encrypt(String data);
    String decrypt(String encryptedData);
}
