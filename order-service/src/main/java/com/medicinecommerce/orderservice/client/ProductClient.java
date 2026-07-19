package com.medicinecommerce.orderservice.client;

import com.medicinecommerce.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${clients.product-service.url}")
public interface ProductClient {
    @GetMapping("/api/v1/products/{productId}")
    ProductResponse getById(@PathVariable Long productId);
}
