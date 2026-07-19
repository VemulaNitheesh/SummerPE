package com.medicinecommerce.orderservice.exception;

public class DownstreamServiceUnavailableException extends RuntimeException {
    public DownstreamServiceUnavailableException(String serviceName) { super(serviceName + " is currently unavailable"); }
}
