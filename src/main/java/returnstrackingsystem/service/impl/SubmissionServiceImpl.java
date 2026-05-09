package returnstrackingsystem.service.impl;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.domain.enums.DocumentStatus;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.dtos.request.SubmissionRequest;
import returnstrackingsystem.exception.*;
import returnstrackingsystem.domain.Document;
import returnstrackingsystem.domain.enums.SubmissionStatus;
import returnstrackingsystem.domain.ReturnDefinition;
import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.domain.TaskTrack;
import returnstrackingsystem.repository.DocumentRepository;
import returnstrackingsystem.repository.ReturnDefinitionRepository;
import returnstrackingsystem.repository.SubmissionRepository;
import returnstrackingsystem.repository.TaskTrackRepository;
import returnstrackingsystem.service.DocStoreService;
import returnstrackingsystem.service.EmailService;
import returnstrackingsystem.service.SubmissionService;
import returnstrackingsystem.util.MailProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static returnstrackingsystem.domain.enums.SubmissionStatus.*;
import static returnstrackingsystem.util.TimeUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final DocumentRepository documentRepository;
    private final TaskTrackRepository taskTrackRepository;
    private final DocStoreService docStoreService;
    private final EmailService emailService;
    @Qualifier("appGraphClient")
    private final GraphServiceClient<?> appGraphClient;
    private final ReturnDefinitionRepository returnDefinitionRepository;

    @Value("${spring.mail.from}")
    private String from;

    @Override
    @Transactional
    public Submission createSubmission(Long returnDefinitionId, SubmissionRequest submissionDTO) {
        requireNonNull(submissionDTO, "Submission DTO cannot be null");

        ReturnDefinition report = returnDefinitionRepository.findById(returnDefinitionId)
                .orElseThrow(
                        () -> new RecordNotFoundException(
                                format("Return definition with id %s not found", returnDefinitionId)));

        OffsetDateTime periodStart = convertToHarareOffsetDateTime(submissionDTO.getPeriodStart());
        OffsetDateTime periodEnd = convertToHarareOffsetDateTime(submissionDTO.getPeriodEnd());
        OffsetDateTime dueAt = convertToHarareOffsetDateTime(submissionDTO.getDueAt());

        Optional<Submission> existingSubmission = submissionRepository
                .findByReturnDefinitionAndPeriodDetails(
                        report,
                        periodStart,
                        periodEnd,
                        dueAt);

        // Clear documentId from ReturnDefinition using direct update
        returnDefinitionRepository.clearDocumentId(report.getId());
        log.info("Cleared documentId from ReturnDefinition: {}", report.getId());
        log.info("ReturnDefinition: {}", report);

        if (existingSubmission.isPresent()) {
            log.info("Submission already exists for ReturnDefinition: {}," +
                    " with same period details, returning existing submission",
                    report.getTitle());

            Submission existing = existingSubmission.get();

            documentRepository.detachDocumentsFromSubmission(existing);

            existing.setStatus(PENDING);
            existing.setReturnDefinition(report);
            Submission savedSubmission = submissionRepository.save(existing);

            log.info("Cleaned up documents and reset submission ID: {}", savedSubmission.getId());
            return savedSubmission;
        }

        log.info("Creating new submission");
        Submission submission = Submission.builder()
                .dueAt(dueAt)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
        submission.setReturnDefinition(report);

        log.info("Saving new submission");
        Submission savedSubmission = submissionRepository.save(submission);

        // Create TodoTask and Calendar event for the new submission
        createTaskAndEventForSubmission(
                savedSubmission,
                savedSubmission
                        .getReturnDefinition()
                        .getResponsiblePerson()
                        .getEmail());
        log.info("Created TodoTask and Calendar event for new submission");

        return savedSubmission;
    }

    @Override
    public Page<Submission> getAllSubmissions(@Param("status") SubmissionStatus status,
            @Param("departmentName") String departmentName,
            @Param("frequency") Frequency frequency,
            @Param("withinDays") Integer withinDays,
            Pageable pageable) {
        log.info("Getting all submissions with filters - status: {}, department: {}, frequency: {}, withinDays: {}",
                status, departmentName, frequency, withinDays);

        OffsetDateTime fromDate = null;
        OffsetDateTime toDate = null;
        List<SubmissionStatus> excludeStatuses = null;

        if (withinDays != null) {
            fromDate = OffsetDateTime.now();
            toDate = fromDate.plusDays(withinDays);
            excludeStatuses = List.of(SubmissionStatus.CLOSED, SubmissionStatus.SUBMITTED);
        }

        return submissionRepository.getAll(status, departmentName, frequency, fromDate, toDate, excludeStatuses,
                pageable);
    }

    @Override
    public Submission getSubmissionById(Long id) {
        requireNonNull(id, "Submission id cannot be null");
        log.info("Getting submission with id: {}", id);
        return submissionRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException(
                        format("Submission with id: %d, not found", id)));
    }

    @Override
    public Submission updateSubmission(Long id, SubmissionRequest submissionDTO) {
        requireNonNull(submissionDTO, "Submission DTO cannot be null");
        requireNonNull(id, "Submission id cannot be null");

        log.info("Updating submission with id: {}", id);
        var submission = getSubmissionById(id);
        submission.setDueAt(convertToHarareOffsetDateTime(submissionDTO.getDueAt()));
        submission.setPeriodStart(convertToHarareOffsetDateTime(submissionDTO.getPeriodStart()));
        submission.setPeriodEnd(convertToHarareOffsetDateTime(submissionDTO.getPeriodEnd()));
        log.info("Saving updated submission");
        return submissionRepository.save(submission);
    }

    @Override
    public boolean deleteSubmission(Long id) {
        requireNonNull(id, "Submission id cannot be null");
        log.info("Deleting Submission with id: {}", id);

        return submissionRepository.findById(id)
                .map(submission -> {
                    if (submission.isDeleted()) {
                        log.warn("Attempted to delete Submission with ID: {}" +
                                " which is already deleted", id);
                        return false;
                    }
                    submission.setDeleted(true);
                    submissionRepository.save(submission);
                    log.info("Submission with ID: {} marked as deleted", id);
                    return true;
                })
                .orElse(false);

    }

    @Override
    @Transactional
    public void attachDocumentAndStartReminder(Long submissionId, MultipartFile file, String ownerEmail) {
        requireNonNull(file, "file is required");

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        log.info("Uploading document: {}, for submission", submission.getReturnDefinition().getTitle());

        ReturnDefinition returnDefinition = submission.getReturnDefinition();

        Optional<Document> existingDocOpt = documentRepository.findBySubmission(submission);

        String uri = docStoreService.save(file);

        if (existingDocOpt.isPresent()) {
            Document existingDoc = existingDocOpt.get();

            docStoreService.delete(existingDoc.getStorageUrl());
            existingDoc.setSubmission(submission);
            existingDoc.setStatus(DocumentStatus.PENDING_VERIFICATION);
            existingDoc.setFileName(file.getOriginalFilename());
            existingDoc.setStorageUrl(uri);
            existingDoc.setContentType(file.getContentType());
            existingDoc.setFileType(file.getContentType());
            existingDoc.setUploadedBy(ownerEmail);
            existingDoc.setUploadedAt(OffsetDateTime.now());

            documentRepository.save(existingDoc);
            var savedDocument = documentRepository.save(existingDoc);
            returnDefinition.setDocumentId(savedDocument.getId());
        } else {
            Document document = Document.builder()
                    .submission(submission)
                    .status(DocumentStatus.PENDING_VERIFICATION)
                    .fileName(file.getOriginalFilename())
                    .storageUrl(uri)
                    .contentType(file.getContentType())
                    .fileType(file.getContentType())
                    .uploadedBy(ownerEmail)
                    .uploadedAt(OffsetDateTime.now())
                    .build();

            var savedDocument = documentRepository.save(document);
            returnDefinition.setDocumentId(savedDocument.getId());
        }

        if (submission.getStatus() == PENDING) {
            submission.setStatus(UPLOADED);
        }
        if (submission.getStatus() == UPLOADED) {
            var savedReturnDefinition = returnDefinitionRepository.save(returnDefinition);
            submission.setReturnDefinition(savedReturnDefinition);
            submissionRepository.save(submission);

            String returnDefinitionTitle = submission.getReturnDefinition().getTitle();
            String body = emailService.buildApprovalNotificationEmail(returnDefinitionTitle);
            String toEmail = submission.getReturnDefinition()
                    .getDepartment()
                    .getHeadOfDepartmentEmail();
            emailService.send(from, toEmail,
                    null,
                    "ReturnDefinition Uploaded",
                    body);
        }

        // TodoTask creation now happens in createSubmission, not here
        // This method only handles document upload
    }

    @Override
    public void sendToRegulatorAndClose(MailProperties mailProperties,
            Long submissionId,
            List<String> cc) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        // Load latest document
        Document document = documentRepository.findAll().stream()
                .filter(foundDocument -> foundDocument.getSubmission().equals(submission))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No document attached"));

        // Validate regulator email
        String regulator = submission.getReturnDefinition().getRegulatoryEmail();
        if (regulator == null || regulator.isBlank()) {
            throw new InvalidEmailException(
                    format("Regulator email not found for regulatory report: %s",
                            submission.getReturnDefinition().getTitle()));
        }

        // Read document bytes
        byte[] bytes = readDocumentBytes(document);

        InputStreamSource src = () -> new ByteArrayInputStream(bytes);
        String body = emailService.buildSubmissionNoticeEmail(
                submission.getReturnDefinition().getTitle(),
                submission.periodLabel());

        emailService.sendWithAttachment(
                mailProperties,
                regulator,
                cc != null ? cc : List.of(),
                "Submission Notice – " + submission.getReturnDefinition().getTitle() +
                        " [" + submission.periodLabel() + "]",
                body,
                document.getFileName(),
                document.getContentType(),
                src);

        submission.setStatus(SUBMITTED);
        submissionRepository.save(submission);

        completeTaskAndEvent(submission);

        createNextRecurringSubmission(submission);
    }

    private void completeTaskAndEvent(Submission submission) {
        taskTrackRepository.findBySubmission(submission).ifPresent(track -> {
            try {
                // Complete Microsoft To Do Task
                if (track.getMsTaskId() != null) {
                    try {
                        TodoTask patch = new TodoTask();
                        patch.status = TaskStatus.COMPLETED;
                        appGraphClient.users(track.getUserEmail())
                                .todo().lists("Tasks").tasks(track.getMsTaskId())
                                .buildRequest().patch(patch);
                        log.info("Completed MS To Do task for user: {}", track.getUserEmail());
                    } catch (Exception e) {
                        log.warn("Failed to complete MS To Do task for user: {}", track.getUserEmail(), e);
                    }
                }

                // Delete Microsoft Calendar Event
                if (track.getMsEventId() != null) {
                    try {
                        appGraphClient.users(track.getUserEmail())
                                .events(track.getMsEventId())
                                .buildRequest().delete();
                        log.info("Deleted MS Calendar event for user: {}", track.getUserEmail());
                    } catch (Exception e) {
                        log.warn("Failed to delete MS Calendar event for user: {}", track.getUserEmail(), e);
                    }
                }

                // Update track record
                track.setCompleted(true);
                track.setCompletedAt(OffsetDateTime.now());
                taskTrackRepository.save(track);
                log.info("Marked task track as completed for submission: {}", submission.getId());

            } catch (Exception e) {
                log.error("Error completing task and event for submission: {}", submission.getId(), e);
            }
        });
    }

    private byte[] readDocumentBytes(Document document) {
        try {
            String storageUrl = String.valueOf(docStoreService.resolveFilePath(document.getStorageUrl()));

            if (storageUrl == null || storageUrl.isBlank()) {
                throw new IllegalStateException("Document storage URL is null or empty");
            }

            Path filePath;
            if (storageUrl.startsWith("file://")) {
                filePath = Path.of(URI.create(storageUrl));
            } else if (storageUrl.contains("://")) {
                throw new UnsupportedOperationException("Non-file URLs not supported yet: " + storageUrl);
            } else {
                filePath = Path.of(storageUrl);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read document from: " + document.getStorageUrl(), e);
        }
    }

    @Override
    @Transactional
    public void flagOverdueSubmissions() {
        OffsetDateTime tenMinutesAgo = OffsetDateTime.now().minusMinutes(10);

        List<Submission> overdueSubmissions = submissionRepository
                .findByDueAtBeforeAndStatusInExcludingSubmitted(tenMinutesAgo,
                        List.of(PENDING, UPLOADED, SubmissionStatus.OVERDUE, UPLOADED_OVERDUE));

        if (overdueSubmissions.isEmpty()) {
            return;
        }

        List<Submission> toUpdate = new ArrayList<>();
        List<Submission> overduePending = new ArrayList<>();
        List<Submission> overdueUploaded = new ArrayList<>();

        overdueSubmissions.forEach(submission -> {
            SubmissionStatus currentStatus = submission.getStatus();

            // Determine new status based on current status
            if (currentStatus == UPLOADED || currentStatus == UPLOADED_OVERDUE) {
                submission.setStatus(UPLOADED_OVERDUE);
                overdueUploaded.add(submission);
                log.debug("Flagged submission {} as UPLOADED_OVERDUE (due at: {})",
                        submission.getId(), submission.getDueAt());
            } else {
                submission.setStatus(SubmissionStatus.OVERDUE);
                overduePending.add(submission);
                log.debug("Flagged submission {} as OVERDUE (due at: {})",
                        submission.getId(), submission.getDueAt());
            }
            toUpdate.add(submission);
        });

        if (!toUpdate.isEmpty()) {
            submissionRepository.saveAll(toUpdate);
        }

        if (!overduePending.isEmpty()) {
            log.info("Flagged {} PENDING submissions as OVERDUE", overduePending.size());
        }
        if (!overdueUploaded.isEmpty()) {
            log.info("Flagged {} UPLOADED submissions as OVERDUE", overdueUploaded.size());
        }
    }

    @Override
    @Transactional
    public void sendToRegulatorViaOutlook(Long submissionId,
            List<String> cc,
            String currentUserEmail,
            String microsoftToken) {

        log.info("Sending submission {} to regulator via Outlook (user: {})",
                submissionId, currentUserEmail);

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RecordNotFoundException(
                        format("Submission with id: %d not found", submissionId)));

        Document document = documentRepository
                .findTopBySubmissionOrderByUploadedAtDesc(submission)
                .orElseThrow(() -> new RecordNotFoundException(
                        "No document attached to submission"));

        String regulatorEmail = submission.getReturnDefinition()
                .getRegulatoryEmail();

        if (regulatorEmail == null || regulatorEmail.isBlank()) {
            throw new InvalidEmailException(
                    format("Regulator email not found for regulatory report: %s",
                            submission.getReturnDefinition().getTitle()));
        }

        try {
            String body = emailService.buildSubmissionNoticeEmail(
                    submission.getReturnDefinition().getTitle(),
                    submission.periodLabel());

            sendOutlookEmail(submission, document, regulatorEmail, cc, body, currentUserEmail, microsoftToken);

            submission.setStatus(SUBMITTED);
            submissionRepository.save(submission);

            completeTaskAndEvent(submission);

            createNextRecurringSubmission(submission);

            log.info("Successfully sent submission {} to regulator via Outlook", submissionId);

        } catch (Exception e) {
            log.error("Failed to send submission {} via Outlook: {}", submissionId, e.getMessage(), e);
            throw new EmailSendException("Failed to send email via Outlook: " + e.getMessage());
        }
    }

    private void sendOutlookEmail(Submission submission,
            Document document,
            String regulatorEmail,
            List<String> cc,
            String emailBody,
            String currentUserEmail,
            String microsoftAccessToken) {

        try {
            // Read document bytes FIRST - fix potential NPE
            byte[] documentBytes;
            try {
                documentBytes = readDocumentBytes(document);
                if (documentBytes.length == 0) {
                    throw new IllegalStateException("Document is empty or could not be read");
                }
                log.debug("Successfully read document: {} bytes", documentBytes.length);
            } catch (Exception e) {
                log.error("Failed to read document bytes: {}", e.getMessage(), e);
                throw new EmailSendException("Failed to read document: " + e.getMessage());
            }

            // Build the email message
            Message message = new Message();

            // Subject
            message.subject = "Submission Notice – " + submission.getReturnDefinition().getTitle() +
                    " [" + submission.periodLabel() + "]";

            // Body
            ItemBody body = new ItemBody();
            body.contentType = BodyType.HTML;
            body.content = emailBody;
            message.body = body;

            // TO recipient
            LinkedList<Recipient> toRecipients = new LinkedList<>();
            Recipient toRecipient = new Recipient();
            EmailAddress toAddress = new EmailAddress();
            toAddress.address = regulatorEmail;
            toRecipient.emailAddress = toAddress;
            toRecipients.add(toRecipient);
            message.toRecipients = toRecipients;

            // CC recipients
            if (cc != null && !cc.isEmpty()) {
                LinkedList<Recipient> ccRecipients = new LinkedList<>();
                for (String ccEmail : cc) {
                    if (ccEmail != null && !ccEmail.trim().isEmpty()) {
                        Recipient ccRecipient = new Recipient();
                        EmailAddress ccAddress = new EmailAddress();
                        ccAddress.address = ccEmail.trim();
                        ccRecipient.emailAddress = ccAddress;
                        ccRecipients.add(ccRecipient);
                    }
                }
                if (!ccRecipients.isEmpty()) {
                    message.ccRecipients = ccRecipients;
                }
            }

            // Create FileAttachment - FIXED ORDER
            FileAttachment attachment = new FileAttachment();
            attachment.oDataType = "#microsoft.graph.fileAttachment"; // Set this FIRST
            attachment.name = document.getFileName();
            attachment.contentType = document.getContentType();
            attachment.contentBytes = documentBytes; // Use the already-read bytes
            attachment.size = documentBytes.length; // Add size

            LinkedList<Attachment> attachmentsList = new LinkedList<>();
            attachmentsList.add(attachment);

            message.attachments = new AttachmentCollectionPage(attachmentsList, null);

            log.debug("Sending email via Outlook with attachment: {}", document.getFileName());
            log.debug("Attachment size: {} bytes", documentBytes.length);
            log.debug("Recipient: {}", regulatorEmail);

            // // Send email
            GraphServiceClient<?> graphClient = createGraphClient(microsoftAccessToken); // Use your method to get
            // client

            graphClient.me()
                    .sendMail(UserSendMailParameterSet
                            .newBuilder()
                            .withMessage(message)
                            .withSaveToSentItems(true)
                            .build())
                    .buildRequest()
                    .post();

            log.info("Outlook email sent successfully for submission: {}", submission.getId());

        } catch (GraphServiceException e) {
            log.error("Graph API Error: Status={}, Error={}",
                    e.getResponseCode(), e.getServiceError(), e);
            handleGraphApiException(e, currentUserEmail);
        } catch (Exception e) {
            log.error("Unexpected error sending Outlook email: {}", e.getMessage(), e);
            throw new EmailSendException("Failed to send email via Outlook: " + e.getMessage());
        }
    }

    private GraphServiceClient<?> createGraphClient(String accessToken) {
        IAuthenticationProvider authProvider = new IAuthenticationProvider() {
            @NotNull
            @Override
            public CompletableFuture<String> getAuthorizationTokenAsync(@NotNull URL url) {
                return CompletableFuture.completedFuture(accessToken);
            }
        };

        return GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    private static String eventDate(OffsetDateTime odt) {
        return odt.atZoneSameInstant(java.time.ZoneId.of("Africa/Harare"))
                .toLocalDateTime().toString();
    }

    private void handleGraphApiException(GraphServiceException e, String userEmail) {
        log.error("Microsoft Graph API error for user {}: {}", userEmail, e.getServiceError(), e);

        if (e.getResponseCode() == 401) {
            throw new TokenExpiredException("Authentication token has expired. Please log in again.");
        } else if (e.getResponseCode() == 403) {
            throw new PermissionDeniedException("You don't have permission to send emails.");
        } else {
            throw new EmailSendException("Failed to send email via Outlook: " + e.getMessage());
        }
    }

    private void createNextRecurringSubmission(Submission submission) {
        try {
            ReturnDefinition returnDefinition = submission.getReturnDefinition();
            Frequency frequency = returnDefinition.getFrequency();

            if (frequency == null) {
                log.warn("Cannot auto-create next submission: Frequency is null for ReturnDefinition {}",
                        returnDefinition.getId());
                return;
            }

            log.info("Auto-creating next submission for ReturnDefinition {} with frequency {}",
                    returnDefinition.getId(), frequency);

            OffsetDateTime nextPeriodStart = calculateNextDate(submission.getPeriodStart(), frequency);
            OffsetDateTime nextPeriodEnd = calculateNextDate(submission.getPeriodEnd(), frequency);
            OffsetDateTime nextDueAt = calculateNextDate(submission.getDueAt(), frequency);

            SubmissionRequest nextSubmissionRequest = SubmissionRequest.builder()
                    .periodStart(nextPeriodStart.atZoneSameInstant(ZoneId.of("Africa/Harare")).toLocalDateTime())
                    .periodEnd(nextPeriodEnd.atZoneSameInstant(ZoneId.of("Africa/Harare")).toLocalDateTime())
                    .dueAt(nextDueAt.atZoneSameInstant(ZoneId.of("Africa/Harare")).toLocalDateTime())
                    .build();

            createSubmission(returnDefinition.getId(), nextSubmissionRequest);

            log.info("Successfully auto-created next recurring submission for ReturnDefinition {}",
                    returnDefinition.getId());

        } catch (Exception e) {
            log.error("Failed to auto-create next recurring submission for submission {}", submission.getId(), e);
        }
    }

    private OffsetDateTime calculateNextDate(OffsetDateTime date, Frequency frequency) {
        if (date == null)
            return null;

        OffsetDateTime nextDate = switch (frequency) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> addMonthsPreservingEndOfMonth(date, 1);
            case QUARTERLY -> addMonthsPreservingEndOfMonth(date, 3);
            case SEMI_ANNUAL -> addMonthsPreservingEndOfMonth(date, 6);
            case YEARLY -> addMonthsPreservingEndOfMonth(date, 12);
        };

        return skipWeekends(nextDate);
    }

    private OffsetDateTime addMonthsPreservingEndOfMonth(OffsetDateTime date, int months) {
        OffsetDateTime newDate = date.plusMonths(months);

        boolean isLastDayOfMonth = date.toLocalDate().lengthOfMonth() == date.getDayOfMonth();

        if (isLastDayOfMonth) {
            return newDate.with(TemporalAdjusters.lastDayOfMonth());
        }

        return newDate;
    }

    private OffsetDateTime skipWeekends(OffsetDateTime date) {
        if (date == null)
            return null;

        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == java.time.DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }

        return date;
    }

    private void createTaskAndEventForSubmission(Submission submission, String ownerEmail) {
        TaskTrack track = taskTrackRepository.findBySubmission(submission)
                .orElseGet(() -> taskTrackRepository.save(TaskTrack.builder()
                        .submission(submission)
                        .userEmail(ownerEmail)
                        .build()));

        if (track.getMsTaskId() == null) {
            TodoTask todoTask = new TodoTask();
            todoTask.title = submission.getReturnDefinition().getTitle() + " – " + submission.periodLabel();
            todoTask.body = new ItemBody();
            todoTask.body.contentType = BodyType.TEXT;
            todoTask.body.content = "Upload & submit report. Due: " + submission.getDueAt();

            todoTask.dueDateTime = new DateTimeTimeZone();
            todoTask.dueDateTime.timeZone = "Africa/Harare";
            todoTask.dueDateTime.dateTime = submission.getDueAt()
                    .atZoneSameInstant(java.time.ZoneId.of("Africa/Harare"))
                    .toLocalDateTime().toString();

            todoTask.reminderDateTime = todoTask.dueDateTime;

            TodoTask created = appGraphClient.users(ownerEmail)
                    .todo().lists("Tasks").tasks()
                    .buildRequest().post(todoTask);

            track.setMsTaskId(created.id);
            taskTrackRepository.save(track);

            if (submission.getReturnDefinition().isSyncCalendar() && track.getMsEventId() == null) {
                Event event = new Event();
                event.subject = "[Compliance] " + submission.getReturnDefinition().getTitle() + " due";
                event.body = new ItemBody();
                event.body.contentType = BodyType.TEXT;
                event.body.content = "Reminder for " + submission.getReturnDefinition().getTitle();

                event.start = new DateTimeTimeZone();
                event.start.timeZone = "Africa/Harare";
                event.start.dateTime = eventDate(submission.getDueAt());

                event.end = new DateTimeTimeZone();
                event.end.timeZone = "Africa/Harare";
                event.end.dateTime = eventDate(submission.getDueAt().plusMinutes(30));

                event.isReminderOn = true;
                event.reminderMinutesBeforeStart = 60;

                Event createdEvt = appGraphClient.users(ownerEmail)
                        .events().buildRequest().post(event);
                track.setMsEventId(createdEvt.id);
                taskTrackRepository.save(track);
            }
        }
    }

}