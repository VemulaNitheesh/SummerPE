package com.medicinecommerce.productservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class VersionController {

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Map.of(
                "service", "product-service",
                "version", "v2"
        );
    }
}