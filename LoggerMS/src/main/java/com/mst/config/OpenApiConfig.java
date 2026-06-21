package com.mst.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loggerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alert Hub - Logger Service")
                        .description("Stores system logs from Processor, Email and SMS services in MongoDB.")
                        .version("1.0.0"));
    }
}
