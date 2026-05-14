package returnstrackingsystem.auth.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.dtos.request.*;
import returnstrackingsystem.dtos.response.AuthResponse;
import returnstrackingsystem.dtos.response.BulkUploadResponse;


public interface AuthService {
    String register(Long departmentId, RegisterRequest request, Authentication loggedInUserAuth);

    AuthResponse login(AuthRequest request);

    void resetPassword(PasswordRequest request, Authentication currentUserAuth);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPasswordWithToken(ResetPasswordRequest request);

    AuthResponse refreshToken(String refreshToken);

    Page<User> getUsers(Pageable pageable);

    BulkUploadResponse bulkRegister(MultipartFile file, Authentication loggedInUserAuth);
    returnstrackingsystem.dtos.response.UserResponse updateUser(Long userId, UpdateUserRequest request, Authentication loggedInUserAuth);
}
