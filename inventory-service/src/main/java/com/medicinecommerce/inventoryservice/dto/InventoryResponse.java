package com.medicinecommerce.inventoryservice.dto;

public record InventoryResponse(Long id, Long productId, Integer availableQuantity, Integer reservedQuantity) { }
