package com.medicinecommerce.inventoryservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI inventoryServiceOpenApi() { return new OpenAPI().info(new Info().title("Medicine E-commerce Inventory Service API").version("v1").description("Inventory quantities only. No product details or Product Service communication.")); }
}
