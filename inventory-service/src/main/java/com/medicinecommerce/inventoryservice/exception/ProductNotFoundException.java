package com.medicinecommerce.inventoryservice.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) { super("Product not found with id: " + productId); }
}
