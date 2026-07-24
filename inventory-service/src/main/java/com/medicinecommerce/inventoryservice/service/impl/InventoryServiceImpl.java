package com.medicinecommerce.inventoryservice.service.impl;

import com.medicinecommerce.inventoryservice.dto.*;
import com.medicinecommerce.inventoryservice.client.ProductClient;
import com.medicinecommerce.inventoryservice.entity.Inventory;
import com.medicinecommerce.inventoryservice.exception.*;
import com.medicinecommerce.inventoryservice.repository.InventoryRepository;
import com.medicinecommerce.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository repository;
    private final ProductClient productClient;
    public InventoryServiceImpl(InventoryRepository repository, ProductClient productClient) { this.repository = repository; this.productClient = productClient; }
    // NOT_SUPPORTED: verifyProductExists() below is a network call to product-service.
    // repository.save()/existsByProductId() each open their own short-lived
    // transaction internally, so no explicit @Transactional is needed here.
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InventoryResponse create(InventoryRequest request) {
        if (repository.existsByProductId(request.productId())) throw new DuplicateResourceException("Inventory already exists for product id: " + request.productId());
        verifyProductExists(request.productId());
        Inventory inventory = new Inventory(); apply(request, inventory); return toResponse(repository.save(inventory));
    }
    @Override public List<InventoryResponse> getAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Override public InventoryResponse getById(Long id) { return toResponse(findById(id)); }
    @Override public InventoryResponse getByProductId(Long productId) { return toResponse(repository.findByProductId(productId).orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId))); }
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InventoryResponse update(Long id, InventoryRequest request) {
        Inventory inventory = findById(id);
        if (!inventory.getProductId().equals(request.productId()) && repository.existsByProductId(request.productId())) throw new DuplicateResourceException("Inventory already exists for product id: " + request.productId());
        if (!inventory.getProductId().equals(request.productId())) verifyProductExists(request.productId());
        apply(request, inventory); return toResponse(repository.save(inventory));
    }
    @Override @Transactional public void delete(Long id) { repository.delete(findById(id)); }
    @Override @Transactional
    public InventoryResponse reserve(Long productId, QuantityRequest request) {
        Inventory inventory = findWithLockByProductId(productId);
        requireAtLeast(inventory.getAvailableQuantity(), request.quantity(), "available");
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        return toResponse(repository.save(inventory));
    }
    @Override @Transactional
    public InventoryResponse release(Long productId, QuantityRequest request) {
        Inventory inventory = findWithLockByProductId(productId);
        requireAtLeast(inventory.getReservedQuantity(), request.quantity(), "reserved");
        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.quantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.quantity());
        return toResponse(repository.save(inventory));
    }
    @Override @Transactional
    public InventoryResponse deduct(Long productId, QuantityRequest request) {
        Inventory inventory = findWithLockByProductId(productId);
        requireAtLeast(inventory.getReservedQuantity(), request.quantity(), "reserved");
        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.quantity());
        return toResponse(repository.save(inventory));
    }
    private Inventory findById(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id)); }
    private void verifyProductExists(Long productId) {
        try {
            productClient.getProductById(productId);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) throw new ProductNotFoundException(productId);
            throw new ProductServiceUnavailableException();
        } catch (ResourceAccessException ex) {
            throw new ProductServiceUnavailableException();
        } catch (RestClientException ex) {
            throw new ProductServiceUnavailableException();
        }
    }
    private Inventory findWithLockByProductId(Long productId) { return repository.findWithLockByProductId(productId).orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId)); }
    private void requireAtLeast(int current, int requested, String inventoryType) {
        if (current < requested) throw new InsufficientInventoryException("Insufficient " + inventoryType + " quantity for requested operation");
    }
    private void apply(InventoryRequest request, Inventory inventory) { inventory.setProductId(request.productId()); inventory.setAvailableQuantity(request.availableQuantity()); inventory.setReservedQuantity(request.reservedQuantity()); }
    private InventoryResponse toResponse(Inventory i) { return new InventoryResponse(i.getId(), i.getProductId(), i.getAvailableQuantity(), i.getReservedQuantity()); }
}
