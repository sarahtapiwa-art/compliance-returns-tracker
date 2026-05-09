package returnstrackingsystem.customvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import returnstrackingsystem.config.EmailDomainProperties;

/**
 * createdBy romeo
 * createdDate 19/11/2025
 * createdTime 10:00
 * projectName compliance-returns-tracker
 **/

@Component
public class NbsEmailValidator implements ConstraintValidator<NbsEmail, String> {

    private final EmailDomainProperties emailDomainProperties;

    public NbsEmailValidator(EmailDomainProperties emailDomainProperties) {
        this.emailDomainProperties = emailDomainProperties;
    }

    @Override
    public void initialize(NbsEmail constraintAnnotation) {
        // Optional initialization
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        String emailLower = email.toLowerCase();
        return emailDomainProperties.getEmailDomains().stream()
                .anyMatch(domain -> emailLower.endsWith("@" + domain.toLowerCase()));
    }
}