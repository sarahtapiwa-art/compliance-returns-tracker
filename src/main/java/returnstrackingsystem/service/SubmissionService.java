package returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.domain.enums.SubmissionStatus;
import returnstrackingsystem.dtos.request.SubmissionRequest;
import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.util.MailProperties;

import java.util.List;

public interface SubmissionService {
        Submission createSubmission(Long returnDefinitionId, SubmissionRequest submissionDTO);

        Submission getSubmissionById(Long id);

        Submission updateSubmission(Long id, SubmissionRequest submissionDTO);

        Page<Submission> getAllSubmissions(@Param("status") SubmissionStatus status,
                        @Param("departmentName") String departmentName,
                        @Param("frequency") Frequency frequency,
                        @Param("withinDays") Integer withinDays,
                        Pageable pageable);

        boolean deleteSubmission(Long id);

        void attachDocumentAndStartReminder(Long submissionId, MultipartFile file, String ownerEmail);

        void sendToRegulatorAndClose(MailProperties mailProperties, Long submissionId,
                        List<String> cc);

        void flagOverdueSubmissions();

        void sendToRegulatorViaOutlook(Long submissionId,
                        List<String> cc,
                        String currentUserEmail,
                        String microsoftToken);

}
