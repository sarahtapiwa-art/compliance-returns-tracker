package returnstrackingsystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 10:46
 * projectName compliance-returns-tracker
 **/

@OpenAPIDefinition(
        info = @Info(
                title = "Compliance Return Tracker Rest APIs",
                description = "API for tracking compliance return reports",
                version = "1.0",
                contact = @Contact(
                        name = "ICT Dev",
                        email = "ict-development@nbs.co.zw"
                ),
                license = @License(
                        name = "BANK123"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "API for managing Outlook calendar events",
                url = "http://localhost:18000/swagger-ui/index.html"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token authentication"
)
public class OpenAPIConfig {
}

