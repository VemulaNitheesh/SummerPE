package com.medicinecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotBlank @Size(max = 100) String category,
        @NotBlank @Size(max = 150) String manufacturer,
        @Size(max = 2048) @Pattern(regexp = "^(https?://.*)?$", message = "imageUrl must be a valid HTTP(S) URL") String imageUrl) { }
