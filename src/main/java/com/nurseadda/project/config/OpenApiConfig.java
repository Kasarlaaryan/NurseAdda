package com.nurseadda.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nurseAddaOpenAPI() {
        String jwtScheme = "Bearer Authentication";

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtScheme);

        return new OpenAPI()
                .info(new Info()
                        .title("NurseAdda API")
                        .description("""
                                ## Healthcare Workforce Management System

                                ### Authentication Flow
                                1. **Register** → `POST /api/auth/register-client` or `POST /api/auth/register-staff`
                                2. **Verify OTP** → `POST /api/auth/verify-otp`
                                3. **Login** → `POST /api/auth/login` → Get JWT token
                                4. **Use token** → `Authorization: Bearer <token>`

                                ### Roles
                                - **ROLE_USER** (Client) — Create staffing requests, view invoices
                                - **ROLE_STAFF** — Accept assignments, check-in/out, view payments
                                - **ROLE_ADMIN** — Manage assignments, approve staff to client
                                - **ROLE_SUPER_ADMIN** — Manage rate configurations, all admin features
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NurseAdda Team")
                                .email("support@nurseadda.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(jwtScheme, securityScheme));
    }
}
