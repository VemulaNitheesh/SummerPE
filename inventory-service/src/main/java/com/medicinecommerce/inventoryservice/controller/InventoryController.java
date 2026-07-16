package com.medicinecommerce.inventoryservice.controller;

import com.medicinecommerce.inventoryservice.dto.*;
import com.medicinecommerce.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated @RestController @RequestMapping("/api/v1/inventories")
@Tag(name = "Inventory", description = "Inventory quantities only; product details are not stored or fetched")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }
    @Operation(summary = "Create inventory") @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request)); }
    @Operation(summary = "List inventory records") @GetMapping
    public List<InventoryResponse> getAll() { return inventoryService.getAll(); }
    @Operation(summary = "Get inventory by ID") @GetMapping("/{id}")
    public InventoryResponse getById(@PathVariable @Min(1) Long id) { return inventoryService.getById(id); }
    @Operation(summary = "Get inventory by product ID") @GetMapping("/product/{productId}")
    public InventoryResponse getByProductId(@PathVariable @Min(1) Long productId) { return inventoryService.getByProductId(productId); }
    @Operation(summary = "Replace inventory") @PutMapping("/{id}")
    public InventoryResponse update(@PathVariable @Min(1) Long id, @Valid @RequestBody InventoryRequest request) { return inventoryService.update(id, request); }
    @Operation(summary = "Delete inventory") @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) { inventoryService.delete(id); return ResponseEntity.noContent().build(); }
    @Operation(summary = "Reserve available inventory") @PatchMapping("/{productId}/reserve")
    public InventoryResponse reserve(@PathVariable @Min(1) Long productId, @Valid @RequestBody QuantityRequest request) { return inventoryService.reserve(productId, request); }
    @Operation(summary = "Release reserved inventory back to available") @PatchMapping("/{productId}/release")
    public InventoryResponse release(@PathVariable @Min(1) Long productId, @Valid @RequestBody QuantityRequest request) { return inventoryService.release(productId, request); }
    @Operation(summary = "Deduct reserved inventory") @PatchMapping("/{productId}/deduct")
    public InventoryResponse deduct(@PathVariable @Min(1) Long productId, @Valid @RequestBody QuantityRequest request) { return inventoryService.deduct(productId, request); }
}
