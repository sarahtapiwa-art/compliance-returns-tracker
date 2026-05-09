package returnstrackingsystem.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * createdBy romeo
 * createdDate 15/8/2025
 * createdTime 09:48
 * projectName compliance-returns-tracker
 **/

@Configuration
public class ManualEnvLoader {

    @PostConstruct
    public void loadEnv() {
        try (var lines = Files.lines(Paths.get(".env.properties"))) {
            lines.filter(line -> !line.startsWith("#") && !line.trim().isEmpty())
                    .forEach(this::parseAndSetEnvVar);
        } catch (IOException e) {
            System.out.println("No .env.properties file found, using system env vars");
        }
    }

    private void parseAndSetEnvVar(String line) {
        String[] parts = line.split("=", 2);
        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();

            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            System.setProperty(key, value);
        }
    }
}
