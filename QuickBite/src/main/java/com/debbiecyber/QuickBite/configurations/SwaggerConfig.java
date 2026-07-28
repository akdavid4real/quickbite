package com.debbiecyber.QuickBite.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI quickBiteOpenApi() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite API")
                        .description("""
                                QuickBite food-delivery REST API.
                                Built with Spring Boot and SQLite.
                                Use the Authorize button to add your JWT token
                                before testing protected endpoints.""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("QuickBite")
                                .email("admin@quickbite.com"))
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token here. " +
                                        "Get it from POST /api/auth/login. " +
                                        "Do NOT include the 'Bearer ' prefix - " +
                                        "Swagger adds it automatically.")
                        )
                );
    }
}
