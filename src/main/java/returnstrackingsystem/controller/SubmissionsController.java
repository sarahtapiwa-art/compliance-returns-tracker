package returnstrackingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.convertor.SubmissionObjectMapper;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.domain.enums.SubmissionStatus;
import returnstrackingsystem.dtos.request.SendOutlookRequest;
import returnstrackingsystem.dtos.request.SubmissionRequest;
import returnstrackingsystem.dtos.response.MessageResponse;
import returnstrackingsystem.dtos.response.PagedResponse;
import returnstrackingsystem.dtos.response.SubmissionResponse;
import returnstrackingsystem.exception.*;
import returnstrackingsystem.service.SubmissionService;
import returnstrackingsystem.service.UserService;
import returnstrackingsystem.util.MailProperties;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/v1/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('USER')")
@Tag(name = "Submissions API", description = "Manage submissions and related documents")
public class SubmissionsController {

        private final SubmissionService submissionService;
        private final SubmissionObjectMapper submissionObjectMapper;
        private final UserService userService;

        @PostMapping("/{submissionId}/send-outlook")
        @PreAuthorize("permitAll()")
        @Operation(summary = "Send submission via Outlook", description = "Sends the submission document using Microsoft Graph API. Requires Microsoft Graph token in Authorization header.")
        public ResponseEntity<MessageResponse> sendViaOutlook(
                        @PathVariable Long submissionId,
                        @RequestBody SendOutlookRequest request) {

                log.info("Outlook send request - Submission: {}, User: {}", submissionId, request.getEmail());

                try {

                    String microsoftToken = getMicosoftTokenString(request);

                    // 1. Validate user has access to this submission
                        validateUserSubmissionAccess(request.getEmail(), submissionId);

                        // 2. Get CC recipients
                        List<String> cc = request.getRecipients() != null
                                        ? request.getRecipients()
                                        : Collections.emptyList();

                        // 3. Send it via Outlook with a Microsoft token
                        submissionService.sendToRegulatorViaOutlook(submissionId, cc, request.getEmail(),
                                        microsoftToken);

                        // 4. Get submission for response
                        var submission = submissionService.getSubmissionById(submissionId);

                        // 5. Create response
                        MessageResponse response = MessageResponse.builder()
                                        .message("Submission sent successfully via Outlook")
                                        .details(Map.of(
                                                        "submissionId", submissionId,
                                                        "recipient",
                                                        submission.getReturnDefinition().getRegulatoryEmail(),
                                                        "sentBy", request.getEmail(),
                                                        "timestamp", OffsetDateTime.now()))
                                        .build();

                        log.info("Outlook email sent successfully for submission: {}", submissionId);
                        return ResponseEntity.ok(response);

                } catch (AuthenticationRequiredException | TokenExpiredException e) {
                        log.warn("Authentication failed: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(MessageResponse.error(
                                                        "Microsoft authentication failed: " + e.getMessage(), 401));

                } catch (PermissionDeniedException e) {
                        log.warn("Access denied for user {} to submission {}", request.getEmail(), submissionId);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(MessageResponse.error("Access denied: " + e.getMessage(), 403));

                } catch (RecordNotFoundException e) {
                        log.warn("Resource not found: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(MessageResponse.error(e.getMessage(), 404));

                } catch (InvalidEmailException e) {
                        log.warn("Invalid email: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(MessageResponse.error(e.getMessage(), 400));

                } catch (EmailSendException e) {
                        log.error("Failed to send email via Outlook: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(MessageResponse.error("Failed to send email: " + e.getMessage(), 500));

                } catch (Exception e) {
                        log.error("Unexpected error: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(MessageResponse.error("An unexpected error occurred", 500));
                }
        }

    @NotNull
    private static String getMicosoftTokenString(SendOutlookRequest request) {
        String microsoftToken = request.getMicrosoftToken();
        if (microsoftToken == null || microsoftToken.isBlank()) {
                throw new AuthenticationRequiredException("Microsoft token is required");
        }

        // Remove "Bearer " prefix if present
        if (microsoftToken.startsWith("Bearer ")) {
                microsoftToken = microsoftToken.substring(7);
        }
        return microsoftToken;
    }

    @Operation(summary = "Create a new submission")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Submission created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input")
        })
        @PostMapping
        public ResponseEntity<SubmissionResponse> createSubmission(
                        @RequestParam(value = "returnDefinitionId") Long returnDefinitionId,
                        @Parameter(description = "Submission request payload", required = true) @Valid @RequestBody SubmissionRequest submissionDTO) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(submissionObjectMapper.toSubmissionResponse(
                                                submissionService.createSubmission(returnDefinitionId,
                                                                submissionDTO)));
        }

        @Operation(summary = "Get all submissions with pagination")
        @GetMapping
        public ResponseEntity<PagedResponse<SubmissionResponse>> getAllSubmissions(
                        @Parameter(description = "Submission status", example = "SUBMITTED")
                        @RequestParam(value = "status", required = false) SubmissionStatus status,
                        @Parameter(description = "Department Name", example = "Operations")
                        @RequestParam(value = "departmentName", required = false) String departmentName,
                        @Parameter(description = "Frequency", example = "WEEKLY")
                        @RequestParam(value = "frequency", required = false) Frequency frequency,
                        @Parameter(description = "Filter upcoming submissions within N days (excludes CLOSED/SUBMITTED)", example = "7")
                        @RequestParam(value = "dueWithin", required = false) Integer dueWithin,
                        @Parameter(description = "Pagination information")
                        @PageableDefault(direction = DESC, sort = "id") Pageable pageable) {
                var page = submissionService.getAllSubmissions(
                                status,
                                departmentName,
                                frequency,
                                dueWithin,
                                pageable)
                                .map(submissionObjectMapper::toSubmissionResponse);

                return ResponseEntity.ok(
                                PagedResponse.<SubmissionResponse>builder()
                                                .content(page.getContent())
                                                .pageNumber(page.getNumber())
                                                .pageSize(page.getSize())
                                                .totalElements(page.getTotalElements())
                                                .totalPages(page.getTotalPages())
                                                .last(page.isLast())
                                                .build());
        }

        @Operation(summary = "Get submission by ID")
        @GetMapping("/{id}")
        public ResponseEntity<SubmissionResponse> getSubmissionById(
                        @Parameter(description = "ID of the submission", required = true) @PathVariable Long id) {
                return ResponseEntity.ok(submissionObjectMapper.toSubmissionResponse(
                                submissionService.getSubmissionById(id)));
        }

        @Operation(summary = "Update a submission by ID")
        @PutMapping("/{id}")
        public ResponseEntity<SubmissionResponse> updateSubmission(
                        @Parameter(description = "ID of the submission", required = true) @PathVariable Long id,
                        @Parameter(description = "Submission request payload", required = true) @Valid @RequestBody SubmissionRequest submissionDTO) {
                return ResponseEntity.ok(submissionObjectMapper.toSubmissionResponse(
                                submissionService.updateSubmission(id, submissionDTO)));
        }

        @Operation(summary = "Delete a submission by ID")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteSubmission(
                        @Parameter(description = "ID of the submission", required = true) @PathVariable Long id) {
                return submissionService.deleteSubmission(id)
                                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        @Operation(summary = "Attach a document to a submission and start reminder", description = "Uploads a file and associates it with the given submission ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Document attached successfully")
        })
        @PostMapping(value = "/{id}/attach", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<String> attachDocumentAndStartReminder(
                        @Parameter(description = "ID of the submission", required = true) @PathVariable("id") Long submissionId,
                        @Parameter(description = "File to upload", required = true) @RequestPart("file") MultipartFile file,
                        @Parameter(description = "Email of the person attaching the document", required = true) @RequestPart("attachingPersonEmail") String attachingPersonEmail) {

                submissionService.attachDocumentAndStartReminder(submissionId, file, attachingPersonEmail);
                return ResponseEntity.ok("Document attached and reminders started");
        }

        @Operation(summary = "Send submission to regulator and close", description = "Sends submission to regulator using provided email credentials and closes the submission")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Submission sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid mail properties or submission ID")
        })
        @PostMapping("/{id}/send")
        public ResponseEntity<String> sendToRegulatorAndClose(
                        @Parameter(description = "Dynamic mail properties for sending email", required = true) @RequestBody @Valid MailProperties mailProperties,
                        @Parameter(description = "ID of the submission", required = true) @PathVariable("id") Long submissionId,
                        @Parameter(description = "List of CC email addresses. Example: cc=user1@example.com&cc=user2@example.com", example = "[\"user1@example.com\", \"user2@example.com\"]") @RequestParam(required = false) List<String> cc) {

                submissionService.sendToRegulatorAndClose(mailProperties, submissionId, cc);
                return ResponseEntity.ok("Submission sent to regulator and closed");
        }

        private void validateUserSubmissionAccess(String userEmail, Long submissionId) {
                try {
                        User user = userService.findUserByEmail(userEmail);

                        var submission = submissionService.getSubmissionById(submissionId);

                        String submissionDept = submission.getReturnDefinition().getDepartment().getDepartmentName();
                        String userDept = user.getDepartment().getDepartmentName();

                        if (!submissionDept.equals(userDept)) {
                                throw new PermissionDeniedException(
                                                String.format("User from %s cannot access submission from %s",
                                                                userDept, submissionDept));
                        }

                        // Option 3: Log the access for audit
                        log.info("User {} accessed submission {}", userEmail, submissionId);

                } catch (RecordNotFoundException e) {
                        throw new PermissionDeniedException("Cannot access submission: " + e.getMessage());
                }
        }
}
