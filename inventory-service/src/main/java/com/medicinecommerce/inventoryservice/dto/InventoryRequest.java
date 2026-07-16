package com.medicinecommerce.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequest(
        @NotNull @Min(1) Long productId,
        @NotNull @Min(0) Integer availableQuantity,
        @NotNull @Min(0) Integer reservedQuantity) { }
