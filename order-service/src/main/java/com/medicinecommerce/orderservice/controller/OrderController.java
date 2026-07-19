package com.medicinecommerce.orderservice.controller;

import com.medicinecommerce.orderservice.dto.OrderRequest;
import com.medicinecommerce.orderservice.dto.OrderResponse;
import com.medicinecommerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated @RestController @RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order orchestration across Product and Inventory services")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }
    @Operation(summary = "Create an order and reserve inventory") @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }
    @Operation(summary = "List all orders") @GetMapping
    public List<OrderResponse> getAll() { return orderService.getAll(); }
    @Operation(summary = "Get an order by ID") @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable @Min(1) Long id) { return orderService.getById(id); }
    @Operation(summary = "Delete an order and release its reserved inventory") @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) { orderService.delete(id); return ResponseEntity.noContent().build(); }
}
