package com.fintech.wcm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI 3.0 documentation.
 * Sets up Swagger UI for the API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workingCapitalManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Working Capital Management API")
                        .description("Enterprise-grade Working Capital Management application API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fintech Team")
                                .email("support@fintech-wcm.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://fintech-wcm.com")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}
