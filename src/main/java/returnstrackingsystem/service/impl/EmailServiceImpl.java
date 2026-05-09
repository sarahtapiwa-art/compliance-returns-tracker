package returnstrackingsystem.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import returnstrackingsystem.service.EmailService;
import returnstrackingsystem.util.MailProperties;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import static java.text.MessageFormat.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${email.approval.message}")
    private String approvalMessage;

    @Value("${email.verified.message}")
    private String verifiedMessage;

    @Value("${email.rejected.message}")
    private String rejectedMessage;

    @Override
    public String buildRegistrationNotificationEmail(String username, String password, String loginUrl) {
        Context context = new Context();

        context.setVariable("username", username);
        context.setVariable("password", password);
        context.setVariable("loginUrl", loginUrl);

        return templateEngine.process("email/registration", context);
    }

    @Override
    public String buildForgotPasswordEmailNotification(String username, String resetLink) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("resetLink", resetLink);

        return templateEngine.process("email/forgot_password", context);
    }

    @Override
    public String buildPasswordResetNotificationEmail(String username, String updateTimestamp) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("updateTimestamp", updateTimestamp);
        return templateEngine.process("email/reset_password", context);
    }

    @Override
    public String buildApprovalNotificationEmail(String reportTitle) {
        Context context = new Context();
        String formattedMessage = format(approvalMessage, reportTitle);
        context.setVariable("formattedMessage", formattedMessage);
        return templateEngine.process("email/verify_document", context);
    }

    @Override
    public String buildRejectedNotificationEmail(String reportTitle) {
        Context context = new Context();
        String formattedMessage = format(rejectedMessage, reportTitle);
        context.setVariable("formattedMessage", formattedMessage);
        return templateEngine.process("email/rejected_document", context);

    }

    @Override
    public String buildVerifiedNotificationEmail(String reportTitle) {
        Context context = new Context();
        String formattedMessage = format(verifiedMessage, reportTitle);
        context.setVariable("formattedMessage", formattedMessage);
        return templateEngine.process("email/verified_document", context);
    }

    @Override
    public String buildOverdueReportEmail(String reportTitle, String dueDate,
            String status) {
        Context context = new Context();
        context.setVariable("reportTitle", reportTitle);
        context.setVariable("dueDate", dueDate);
        context.setVariable("status", status);
        return templateEngine.process("email/overdue_report", context);
    }

    @Override
    public void send(String from, String to, List<String> cc, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(msg, false);
            mimeMessageHelper.setTo(to);

            if (cc != null && !cc.isEmpty()) {
                mimeMessageHelper.setCc(cc.toArray(new String[0]));
            }

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Email send failed", e);
        }
    }

    @Override
    public String buildSubmissionNoticeEmail(String reportTitle, String periodLabel) {
        Context context = new Context();
        context.setVariable("reportTitle", reportTitle);
        context.setVariable("periodLabel", periodLabel);
        context.setVariable("logoBase64", getLogoAsBase64());
        return templateEngine.process("email/submission_notice", context);
    }

    @Override
    public void sendWithAttachment(MailProperties mailProperties, String to, List<String> cc,
            String subject, String htmlBody,
            String attachmentFilename, String contentType,
            InputStreamSource attachment) {
        try {
            JavaMailSenderImpl dynamicMailSender = new JavaMailSenderImpl();
            dynamicMailSender.setHost("smtp.office365.com");
            dynamicMailSender.setPort(587);
            dynamicMailSender.setUsername(mailProperties.getUsername());
            dynamicMailSender.setPassword(mailProperties.getPassword());

            Properties props = dynamicMailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "false");

            MimeMessage msg = dynamicMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(new InternetAddress(mailProperties.getUsername(), "National Building Society"));
            helper.setTo(to);

            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }

            helper.setSubject(subject);

            helper.setText(htmlBody, true);

            helper.addAttachment(attachmentFilename, attachment, contentType);

            dynamicMailSender.send(msg);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Email send failed", e);
        }
    }

    private String getLogoAsBase64() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("static/assets/nbs-logo.png");

            if (inputStream == null) {
                throw new RuntimeException("Logo file not found: static/assets/nbs-logo.png");
            }

            byte[] imageBytes = inputStream.readAllBytes();

            long fileSizeKB = imageBytes.length / 1024;
            log.debug("Logo file size: {} KB", fileSizeKB);

            if (fileSizeKB > 2000) {
                log.warn("Logo file size ({}) is very large - Gmail may not display it", fileSizeKB);
            }

            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            base64 = base64.replace("\n", "").replace("\r", "").trim();

            if (base64.isEmpty()) {
                throw new RuntimeException("Generated base64 string is empty");
            }

            log.debug("Base64 length: {} characters", base64.length());
            return base64;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load logo: " + e.getMessage(), e);
        }
    }

}
