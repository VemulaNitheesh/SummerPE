package com.medicinecommerce.productservice.monitoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MonitoringController {

    private final RequestCounter requestCounter;

    public MonitoringController(RequestCounter requestCounter) {
        this.requestCounter = requestCounter;
    }

    @GetMapping("/monitor/active-requests")
    public Map<String, Integer> getActiveRequests() {
        return Map.of(
                "activeRequests",
                requestCounter.getActiveRequests()
        );
    }
}
