package com.beowulf.clinical.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Beowulf - Clinical Order Management System")
                .version("1.0.0")
                .description("CRUD-style API modeling a clinical workflow: patients, orders, studies, and results with result versioning and optimistic locking.")
                .contact(new Contact().name("Beowulf API")));
    }
}
