package com.medicinecommerce.productservice.monitoring;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RequestCounter {

    private final AtomicInteger activeRequests = new AtomicInteger(0);

    public void increment() {
        activeRequests.incrementAndGet();
    }

    public void decrement() {
        activeRequests.decrementAndGet();
    }

    public int getActiveRequests() {
        return activeRequests.get();
    }
}
