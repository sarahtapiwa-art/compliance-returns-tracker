package zw.co.nbs.returnstrackingsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 9/2/2026
 * createdTime 14:40
 * projectName compliance-returns-tracker
 **/

@Data
@Component
@ConfigurationProperties(prefix = "app.registration.allowed")
public class EmailDomainProperties {
    private List<String> emailDomains = new ArrayList<>();
}
