package returnstrackingsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 11:47
 * projectName compliance-returns-tracker
 **/

@Data
@Configuration
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private List<String> allowedOrigins;
}

