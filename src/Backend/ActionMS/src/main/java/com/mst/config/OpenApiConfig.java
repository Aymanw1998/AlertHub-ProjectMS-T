package com.mst.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Current Server")
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return securedApi(
                "Alert Hub - Action Service",
                "Creates, updates, schedules and triggers notification actions."
        );
    }

    private OpenAPI securedApi(String title, String description) {
        String scheme = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(
                        scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
