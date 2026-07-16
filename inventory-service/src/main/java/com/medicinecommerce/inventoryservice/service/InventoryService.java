package com.medicinecommerce.inventoryservice.service;

import com.medicinecommerce.inventoryservice.dto.*;
import java.util.List;

public interface InventoryService {
    InventoryResponse create(InventoryRequest request);
    List<InventoryResponse> getAll();
    InventoryResponse getById(Long id);
    InventoryResponse getByProductId(Long productId);
    InventoryResponse update(Long id, InventoryRequest request);
    void delete(Long id);
    InventoryResponse reserve(Long productId, QuantityRequest request);
    InventoryResponse release(Long productId, QuantityRequest request);
    InventoryResponse deduct(Long productId, QuantityRequest request);
}
