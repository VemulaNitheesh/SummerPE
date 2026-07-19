package com.medicinecommerce.orderservice.service.impl;

import com.medicinecommerce.orderservice.client.InventoryClient;
import com.medicinecommerce.orderservice.client.ProductClient;
import com.medicinecommerce.orderservice.dto.OrderRequest;
import com.medicinecommerce.orderservice.dto.OrderResponse;
import com.medicinecommerce.orderservice.dto.ProductResponse;
import com.medicinecommerce.orderservice.dto.QuantityRequest;
import com.medicinecommerce.orderservice.entity.Order;
import com.medicinecommerce.orderservice.entity.OrderStatus;
import com.medicinecommerce.orderservice.exception.DownstreamServiceUnavailableException;
import com.medicinecommerce.orderservice.exception.InsufficientInventoryException;
import com.medicinecommerce.orderservice.exception.ProductNotFoundException;
import com.medicinecommerce.orderservice.exception.ResourceNotFoundException;
import com.medicinecommerce.orderservice.mapper.OrderMapper;
import com.medicinecommerce.orderservice.repository.OrderRepository;
import com.medicinecommerce.orderservice.service.OrderService;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderServiceImpl(OrderRepository repository, OrderMapper mapper, ProductClient productClient, InventoryClient inventoryClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
    }

    @Override @Transactional
    public OrderResponse create(OrderRequest request) {
        ProductResponse product = getProduct(request.productId());
        reserveInventory(request.productId(), request.quantity());

        Order order = new Order();
        order.setCustomerName(request.customerName().trim());
        order.setCustomerEmail(request.customerEmail().trim());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalPrice(product.price().multiply(BigDecimal.valueOf(request.quantity())));
        order.setStatus(OrderStatus.CONFIRMED);
        return mapper.toResponse(repository.save(order));
    }

    @Override public List<OrderResponse> getAll() { return repository.findAll().stream().map(mapper::toResponse).toList(); }
    @Override public OrderResponse getById(Long id) { return mapper.toResponse(findOrder(id)); }

    @Override @Transactional
    public void delete(Long id) {
        Order order = findOrder(id);
        if (order.getStatus() == OrderStatus.CONFIRMED) releaseInventory(order.getProductId(), order.getQuantity());
        repository.delete(order);
    }

    private ProductResponse getProduct(Long productId) {
        try {
            return productClient.getById(productId);
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException(productId);
        } catch (FeignException ex) {
            throw new DownstreamServiceUnavailableException("Product Service");
        }
    }

//    private void reserveInventory(Long productId, Integer quantity) {
//        try {
//            inventoryClient.reserve(productId, new QuantityRequest(quantity));
//        } catch (FeignException.Conflict ex) {
//            throw new InsufficientInventoryException();
//        } catch (FeignException ex) {
//            throw new DownstreamServiceUnavailableException("Inventory Service");
//        }
//    }
private void reserveInventory(Long productId, Integer quantity) {
    try {
        inventoryClient.reserve(productId, new QuantityRequest(quantity));
    } catch (FeignException.Conflict ex) {
        throw new InsufficientInventoryException();
    } catch (FeignException ex) {

        throw new DownstreamServiceUnavailableException("Inventory Service");
    }
}

    private void releaseInventory(Long productId, Integer quantity) {
        try {
            inventoryClient.release(productId, new QuantityRequest(quantity));
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Inventory not found for product id: " + productId);
        } catch (FeignException ex) {
            throw new DownstreamServiceUnavailableException("Inventory Service");
        }
    }

    private Order findOrder(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }
}
