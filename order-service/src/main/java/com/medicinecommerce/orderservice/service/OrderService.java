package com.medicinecommerce.orderservice.service;

import com.medicinecommerce.orderservice.dto.OrderRequest;
import com.medicinecommerce.orderservice.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse create(OrderRequest request);
    List<OrderResponse> getAll();
    OrderResponse getById(Long id);
    void delete(Long id);
}
