package zw.co.nbs.returnstrackingsystem.auth.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.auth.service.AuthService;
import zw.co.nbs.returnstrackingsystem.convertor.UserObjectMapper;
import zw.co.nbs.returnstrackingsystem.dtos.request.*;
import zw.co.nbs.returnstrackingsystem.dtos.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import zw.co.nbs.returnstrackingsystem.dtos.response.PagedResponse;
import zw.co.nbs.returnstrackingsystem.dtos.response.UserResponse;

import java.util.Map;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 09:18
 * projectName compliance-returns-tracker
 **/

@Validated
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication & registration")
public class AuthController {

        private final AuthService authService;
        private final UserObjectMapper userObjectMapper;

        @Operation(summary = "Register a new user", description = "Only SUPER_SYSTEM_ADMIN and ADMIN can register new users", security = {
                        @SecurityRequirement(name = "bearerAuth") })
        @PostMapping("/register")
        @PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN')")
        public ResponseEntity<String> register(
                        @RequestParam Long departmentId,
                        @RequestBody @Valid RegisterRequest request,
                        Authentication loggedInUserAuth) {

                return ResponseEntity.ok(authService.register(departmentId, request, loggedInUserAuth));
        }

        @Operation(summary = "User login", description = "Returns a JWT token")
        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
                return ResponseEntity.ok(authService.login(request));
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
                authService.forgotPassword(request);
                return ResponseEntity.ok(Map.of("message", "Password reset instructions sent to your email"));
        }

        @Operation(summary = "Reset user forgotten password", description = "Allows password reset for forgotten password")
        @PostMapping("/password-reset")
        public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
                authService.resetPasswordWithToken(request);
                return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        }

        @Operation(summary = "Reset user password", description = "Allows password reset for authorized users", security = {
                        @SecurityRequirement(name = "bearerAuth") })
        @PostMapping("/reset-password")
        public ResponseEntity<String> resetPassword(@RequestBody PasswordRequest request,
                        Authentication authentication) {
                authService.resetPassword(request, authentication);
                return ResponseEntity.ok("Password updated successfully");
        }

        @PostMapping("/refresh")
        @Operation(summary = "Refresh access token using refresh token", description = "Provide a valid refresh token to obtain a new access token. The refresh token itself is returned unchanged.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The refresh token string", required = true, content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "your-refresh-token-here"))), responses = {
                        @ApiResponse(responseCode = "200", description = "New access token generated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
        })
        public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
                return ResponseEntity.ok(authService.refreshToken(refreshToken));
        }

    @GetMapping("/users")
    @Operation(summary = "Get paginated list of users",
            description = "Returns a paginated list of all registered users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @PageableDefault(direction = Sort.Direction.DESC, sort = "id")
            Pageable pageable) {
        Page<UserResponse> page = authService.getUsers(pageable)
                .map(userObjectMapper::toUserResponse);

        return ResponseEntity.ok(
                PagedResponse.<UserResponse>builder()
                        .content(page.getContent())
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build()
        );
    }

    @Operation(summary = "Bulk register new users",
            description = "Only SUPER_SYSTEM_ADMIN and ADMIN can bulk register new users via Excel",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(value = "/bulk-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse> bulkRegister(
            @RequestParam("file") MultipartFile file,
            Authentication loggedInUserAuth) {
        return ResponseEntity.ok(authService.bulkRegister(file, loggedInUserAuth));
    }
}
