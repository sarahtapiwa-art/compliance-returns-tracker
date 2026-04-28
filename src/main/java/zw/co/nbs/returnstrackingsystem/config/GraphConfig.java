package zw.co.nbs.returnstrackingsystem.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 20/8/2025
 * createdTime 11:35
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Configuration
public class GraphConfig {

    @Bean(name = "appGraphClient")
    public GraphServiceClient<?> appGraphClient(
            @Value("${azure.client-id}") String clientId,
            @Value("${azure.client-secret}") String clientSecret,
            @Value("${azure.tenant-id}") String tenantId) {

        log.info("Creating app-only Graph client for background operations");

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                List.of("https://graph.microsoft.com/.default"),
                credential
        );

        return GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

}