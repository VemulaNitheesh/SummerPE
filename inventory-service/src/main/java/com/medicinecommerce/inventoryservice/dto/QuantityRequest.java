package com.medicinecommerce.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuantityRequest(@NotNull @Min(1) Integer quantity) { }
