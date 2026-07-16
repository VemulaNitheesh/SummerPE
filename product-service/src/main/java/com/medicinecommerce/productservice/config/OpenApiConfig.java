package com.medicinecommerce.productservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI productServiceOpenApi() { return new OpenAPI().info(new Info().title("Medicine E-commerce Product Service API").version("v1").description("Catalog APIs. Product stock and inventory are not managed here.")); }
}
