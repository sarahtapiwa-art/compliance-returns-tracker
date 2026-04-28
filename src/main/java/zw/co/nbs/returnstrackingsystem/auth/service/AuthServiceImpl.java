package zw.co.nbs.returnstrackingsystem.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.auth.jwt.JwtUtil;
import zw.co.nbs.returnstrackingsystem.domain.Department;
import zw.co.nbs.returnstrackingsystem.domain.PasswordResetToken;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.domain.enums.Role;
import zw.co.nbs.returnstrackingsystem.dtos.request.*;
import zw.co.nbs.returnstrackingsystem.dtos.response.AuthResponse;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadError;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse;
import zw.co.nbs.returnstrackingsystem.exception.BadRequestException;
import zw.co.nbs.returnstrackingsystem.exception.RecordNotFoundException;
import zw.co.nbs.returnstrackingsystem.repository.DepartmentRepository;
import zw.co.nbs.returnstrackingsystem.repository.UserRepository;
import zw.co.nbs.returnstrackingsystem.service.EmailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 09:46
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenService passwordResetTokenService;

    @Value("${spring.mail.from}")
    private String mailFrom;
    @Value("${system.admin.user.default-password}")
    private String defaultPassword;
    @Value("${user.forgot.password.url}")
    private String resetPasswordUrl;
    @Value("${user.account.creation.url}")
    private String userCreationUrl;

    @Override
    public String register(Long departmentId, RegisterRequest request, Authentication loggedInUserAuth) {
        log.info("Registering user {}", request.username());
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BadRequestException("Username already taken, please try another");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists, please try another");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(
                        () -> new RecordNotFoundException(
                                format("Department with id %d not found", departmentId)));

        Set<Role> requestedRoles = new HashSet<>(request.roles());

        Set<Role> assignableRoles = getAssignableRoles(loggedInUserAuth);

        if (!assignableRoles.containsAll(requestedRoles)) {
            throw new RuntimeException("You are not allowed to assign one or more requested roles");
        }

        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(defaultPassword))
                .department(department)
                .build();

        if (requestedRoles.isEmpty()) {
            requestedRoles.add(Role.USER);
        }

        user.setRoles(requestedRoles);
        var registeredUser = userRepository.save(user);
        log.info("User registered successfully: {}", registeredUser.getUsername());

        log.info("Sending welcome email to: {}", user.getEmail());

        String htmlBody = emailService.buildRegistrationNotificationEmail(
                user.getUsername(),
                defaultPassword,
                userCreationUrl);

        emailService.send(mailFrom,
                user.getEmail(),
                new ArrayList<>(),
                "Welcome to the Compliance Returns Tracker System",
                htmlBody);

        return "User registered successfully";
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsernameWithDepartment(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public void resetPassword(PasswordRequest request, Authentication currentUserAuth) {
        User user = userRepository.findByUsername(currentUserAuth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> currentRoles = currentUserAuth.getAuthorities().stream()
                .map(auth -> Role.valueOf(auth.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toSet());

        if (!currentUserAuth.getName().equals(currentUserAuth.getName())) {
            if (currentRoles.contains(Role.SUPER_SYSTEM_ADMIN)) {
            } else {
                throw new RuntimeException("You are not authorized to reset this user's password");
            }
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password does not match!");
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadRequestException("New password does not match!");
        }
        user.setPassword(passwordEncoder.encode(request.confirmNewPassword()));
        var updatedUser = userRepository.save(user);
        log.info("Password updated for user: {}", updatedUser.getUsername());

        log.info("Sending password reset email to: {}", user.getEmail());

        String timestamp = LocalDateTime.now().format(ofPattern("MMMM d, yyyy hh:mm a"));

        String htmlBody = emailService.buildPasswordResetNotificationEmail(
                user.getUsername(),
                timestamp);

        emailService.send(mailFrom,
                user.getEmail(),
                new ArrayList<>(),
                "Password Updated - Compliance Return Diary",
                htmlBody);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        String resetToken = passwordResetTokenService.generatePasswordResetToken(user);

        String resetLink = resetPasswordUrl + resetToken;

        String htmlBody = emailService.buildForgotPasswordEmailNotification(
                user.getUsername(),
                resetLink);

        emailService.send(
                mailFrom,
                user.getEmail(),
                new ArrayList<>(),
                "Password Reset Request - Compliance Return Diary",
                htmlBody);

        log.info("Password reset token sent to: {}", user.getEmail());
    }

    @Override
    public void resetPasswordWithToken(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenService.validatePasswordResetToken(request.token());

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadRequestException("New password does not match!");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        passwordResetTokenService.deleteToken(resetToken);

        log.info("Password reset successfully for user: {}", user.getUsername());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a"));

        String htmlBody = emailService.buildPasswordResetNotificationEmail(
                user.getUsername(),
                timestamp);

        emailService.send(
                mailFrom,
                user.getEmail(),
                new ArrayList<>(),
                "Password Updated - Compliance Return Diary",
                htmlBody);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        User user = refreshTokenService.validateRefreshToken(refreshToken);
        String accessToken = jwtUtil.generateToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        log.info("Fetching users");
        return userRepository.findAll(pageable);
    }

    @Override
    public BulkUploadResponse bulkRegister(MultipartFile file, Authentication loggedInUserAuth) {
        List<BulkUploadError> errors = new ArrayList<>();
        List<Long> successfulIds = new ArrayList<>();
        int totalProcessed = 0;
        int successfulCount = 0;

        try (Workbook workbook = WorkbookFactory
                .create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row))
                    continue;

                totalProcessed++;
                String email = getCellValue(row.getCell(0)); // Column A: Email

                try {
                    RegisterRequest registrationRequest = mapRowToRegisterRequest(row);
                    String departmentName = getCellValue(row.getCell(2)); // Column C: Department
                    Department department = departmentRepository.findByDepartmentNameIgnoreCase(departmentName)
                            .orElseThrow(() -> new RecordNotFoundException("Department not found: " + departmentName));

                    register(department.getId(), registrationRequest, loggedInUserAuth);

                    User user = userRepository.findByUsername(registrationRequest.username())
                            .orElseThrow(() -> new RuntimeException("User not found after registration"));

                    successfulIds.add(user.getId());
                    successfulCount++;

                } catch (Exception e) {
                    log.error("Failed to register user at row {}: {}", i, e.getMessage());
                    errors.add(zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadError.builder()
                            .index(i)
                            .title(email)
                            .error(e.getMessage())
                            .build());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel file: " + e.getMessage(), e);
        }

        return zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse.builder()
                .totalProcessed(totalProcessed)
                .successfulCount(successfulCount)
                .failedCount(errors.size())
                .errors(errors)
                .successfulIds(successfulIds)
                .build();
    }

    private RegisterRequest mapRowToRegisterRequest(org.apache.poi.ss.usermodel.Row row) {
        String rolesStr = getCellValue(row.getCell(1)); // Column B: Role
        Set<Role> roles = new HashSet<>();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(rolesStr)) {
            for (String role : rolesStr.split(",")) {
                try {
                    roles.add(Role.valueOf(role.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role: {}", role);
                }
            }
        }
        if (roles.isEmpty()) {
            roles.add(Role.USER);
        }

        return new RegisterRequest(
                getCellValue(row.getCell(3)), // Column D: UserName
                getCellValue(row.getCell(0)), // Column A: Email
                roles);
    }

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private boolean isEmptyRow(org.apache.poi.ss.usermodel.Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private Set<Role> getAssignableRoles(Authentication currentUserAuth) {
        if (currentUserAuth == null) {
            return Set.of(Role.SUPER_SYSTEM_ADMIN);
        }

        Collection<? extends GrantedAuthority> authorities = currentUserAuth.getAuthorities();
        Set<Role> roles = authorities.stream()
                .map(auth -> Role.valueOf(auth.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toSet());

        if (roles.contains(Role.SUPER_SYSTEM_ADMIN)) {
            return Set.of(Role.SUPER_SYSTEM_ADMIN, Role.ADMIN, Role.USER);
        } else if (roles.contains(Role.ADMIN)) {
            return Set.of(Role.USER);
        } else {
            return Set.of();
        }
    }
}
