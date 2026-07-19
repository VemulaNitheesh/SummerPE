package com.medicinecommerce.orderservice.dto;

import com.medicinecommerce.orderservice.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(Long id, String customerName, String customerEmail, Long productId,
                            Integer quantity, BigDecimal totalPrice, OrderStatus status, Instant createdAt) { }
