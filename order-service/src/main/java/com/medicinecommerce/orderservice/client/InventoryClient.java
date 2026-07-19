package com.medicinecommerce.orderservice.client;

import com.medicinecommerce.orderservice.dto.QuantityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${clients.inventory-service.url}")
public interface InventoryClient {
    @PatchMapping("/api/v1/inventories/{productId}/reserve")
    void reserve(@PathVariable Long productId, @RequestBody QuantityRequest request);

    @PatchMapping("/api/v1/inventories/{productId}/release")
    void release(@PathVariable Long productId, @RequestBody QuantityRequest request);
}
