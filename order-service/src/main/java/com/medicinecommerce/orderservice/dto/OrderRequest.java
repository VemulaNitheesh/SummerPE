package com.medicinecommerce.orderservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotBlank @Size(max = 150) String customerName,
        @NotBlank @Email @Size(max = 254) String customerEmail,
        @NotNull @Min(1) Long productId,
        @NotNull @Min(1) Integer quantity) { }
