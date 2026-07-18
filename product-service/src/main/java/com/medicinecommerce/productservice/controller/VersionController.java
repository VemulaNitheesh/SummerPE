package com.medicinecommerce.productservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class VersionController {
    private final String version;

    public VersionController(@Value("${app.version:unknown}") String version) {
        this.version = version;
    }


    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Map.of(
                "service", "product-service",
                "version", version
        );
    }
}