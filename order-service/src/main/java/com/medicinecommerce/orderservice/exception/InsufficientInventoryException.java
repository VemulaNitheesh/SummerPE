package com.medicinecommerce.orderservice.exception;

public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException() { super("Insufficient inventory for requested order"); }
}
