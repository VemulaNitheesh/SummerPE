package com.medicinecommerce.productservice.service;

import com.medicinecommerce.productservice.dto.ProductRequest;
import com.medicinecommerce.productservice.dto.ProductResponse;
import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse getById(Long id);
    List<ProductResponse> getAll();
    List<ProductResponse> searchByName(String name);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
