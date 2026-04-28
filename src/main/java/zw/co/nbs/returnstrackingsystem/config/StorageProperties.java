package zw.co.nbs.returnstrackingsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/10/2025
 * createdTime 08:59
 * projectName compliance-returns-tracker
 **/

@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    private List<String> allowedFileTypes;
}
