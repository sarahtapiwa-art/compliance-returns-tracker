package zw.co.nbs.returnstrackingsystem.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * createdBy romeo
 * createdDate 15/8/2025
 * createdTime 09:46
 * projectName compliance-returns-tracker
 **/

@Configuration
public class EnvConfig {
    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
