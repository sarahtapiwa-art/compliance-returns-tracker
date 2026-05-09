package returnstrackingsystem.service;

import org.springframework.core.io.InputStreamSource;
import returnstrackingsystem.util.MailProperties;

import java.util.List;

public interface EmailService {
    String buildRegistrationNotificationEmail(String username, String password, String loginUrl);

    String buildForgotPasswordEmailNotification(String username, String resetLink);

    String buildPasswordResetNotificationEmail(String username, String updateTimestamp);

    String buildApprovalNotificationEmail(String regulatoryReportTitle);

    String buildRejectedNotificationEmail(String regulatoryReportTitle);

    String buildVerifiedNotificationEmail(String regulatoryReportTitle);

    String buildOverdueReportEmail(String reportTitle, String dueDate, String status);

    String buildSubmissionNoticeEmail(String reportTitle, String periodLabel);

    void send(String from, String to, List<String> cc, String subject, String htmlBody);

    void sendWithAttachment(MailProperties mailProperties, String to,
            List<String> cc, String subject, String htmlBody,
            String attachmentFilename, String contentType,
            InputStreamSource attachment);

}