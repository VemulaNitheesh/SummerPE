package com.medicinecommerce.orderservice.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, String description, BigDecimal price,
                              String category, String manufacturer, String imageUrl) { }
