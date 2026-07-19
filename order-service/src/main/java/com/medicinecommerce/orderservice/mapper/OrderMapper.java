package com.medicinecommerce.orderservice.mapper;

import com.medicinecommerce.orderservice.dto.OrderResponse;
import com.medicinecommerce.orderservice.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getCustomerName(), order.getCustomerEmail(), order.getProductId(),
                order.getQuantity(), order.getTotalPrice(), order.getStatus(), order.getCreatedAt());
    }
}
