package com.medicinecommerce.productservice.controller;

import com.medicinecommerce.productservice.dto.*;
import com.medicinecommerce.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated @RestController @RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog management; inventory and stock are intentionally excluded")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @Operation(summary = "Create a product") @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request)); }
    @Operation(summary = "Get a product by ID") @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable @Min(1) Long id) { return productService.getById(id); }
    @Operation(summary = "List all products") @GetMapping
    public List<ProductResponse> getAll() { return productService.getAll(); }
    @Operation(summary = "Search products by name") @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam @NotBlank String name) { return productService.searchByName(name); }
    @Operation(summary = "Replace a product") @PutMapping("/{id}")
    public ProductResponse update(@PathVariable @Min(1) Long id, @Valid @RequestBody ProductRequest request) { return productService.update(id, request); }
    @Operation(summary = "Delete a product") @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) { productService.delete(id); return ResponseEntity.noContent().build(); }
}
