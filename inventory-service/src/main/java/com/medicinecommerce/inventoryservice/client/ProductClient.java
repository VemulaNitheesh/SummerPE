package com.medicinecommerce.inventoryservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** HTTP-only client for the Product Service. */
@Component
public class ProductClient {
    private final RestClient productServiceRestClient;

    public ProductClient(RestClient productServiceRestClient) {
        this.productServiceRestClient = productServiceRestClient;
    }

    public void getProductById(Long productId) {
        productServiceRestClient.get()
                .uri("/api/v1/products/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }
}
