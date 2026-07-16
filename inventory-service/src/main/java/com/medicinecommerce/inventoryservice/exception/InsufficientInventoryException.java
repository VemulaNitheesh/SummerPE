package com.medicinecommerce.inventoryservice.exception;

public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String message) { super(message); }
}
