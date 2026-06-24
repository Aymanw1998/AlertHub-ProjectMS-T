package com.mst.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
    public OpenAPI smsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alert Hub - SMS Service")
                        .description("Consumes SMS notifications from Kafka and sends them through Twilio.")
                        .version("1.0.0"));
    }
}
