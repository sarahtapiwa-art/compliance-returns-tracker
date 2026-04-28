package zw.co.nbs.returnstrackingsystem.auth.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.dtos.request.*;
import zw.co.nbs.returnstrackingsystem.dtos.response.AuthResponse;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 09:21
 * projectName compliance-returns-tracker
 **/

public interface AuthService {
    String register(Long departmentId, RegisterRequest request, Authentication loggedInUserAuth);

    AuthResponse login(AuthRequest request);

    void resetPassword(PasswordRequest request, Authentication currentUserAuth);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPasswordWithToken(ResetPasswordRequest request);

    AuthResponse refreshToken(String refreshToken);

    Page<User> getUsers(Pageable pageable);

    BulkUploadResponse bulkRegister(MultipartFile file, Authentication loggedInUserAuth);
}
