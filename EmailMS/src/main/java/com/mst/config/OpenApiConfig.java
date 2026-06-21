package com.mst.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI emailOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alert Hub - Email Service")
                        .description("Consumes email notifications from Kafka and sends them to recipients.")
                        .version("1.0.0"));
    }
}
