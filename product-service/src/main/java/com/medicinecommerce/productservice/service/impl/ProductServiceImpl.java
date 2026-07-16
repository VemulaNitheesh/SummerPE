package com.medicinecommerce.productservice.service.impl;

import com.medicinecommerce.productservice.dto.*;
import com.medicinecommerce.productservice.entity.Product;
import com.medicinecommerce.productservice.exception.ResourceNotFoundException;
import com.medicinecommerce.productservice.repository.ProductRepository;
import com.medicinecommerce.productservice.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    public ProductServiceImpl(ProductRepository productRepository) { this.productRepository = productRepository; }
    @Override @Transactional public ProductResponse create(ProductRequest request) { Product p = new Product(); apply(request, p); return toResponse(productRepository.save(p)); }
    @Override public ProductResponse getById(Long id) { return toResponse(findProduct(id)); }
    @Override public List<ProductResponse> getAll() { return productRepository.findAll().stream().map(this::toResponse).toList(); }
    @Override public List<ProductResponse> searchByName(String name) { return productRepository.findByNameContainingIgnoreCase(name.trim()).stream().map(this::toResponse).toList(); }
    @Override @Transactional public ProductResponse update(Long id, ProductRequest request) { Product p = findProduct(id); apply(request, p); return toResponse(productRepository.save(p)); }
    @Override @Transactional public void delete(Long id) { productRepository.delete(findProduct(id)); }
    private Product findProduct(Long id) { return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)); }
    private void apply(ProductRequest r, Product p) { p.setName(r.name().trim()); p.setDescription(r.description().trim()); p.setPrice(r.price()); p.setCategory(r.category().trim()); p.setManufacturer(r.manufacturer().trim()); p.setImageUrl(r.imageUrl()); }
    private ProductResponse toResponse(Product p) { return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCategory(), p.getManufacturer(), p.getImageUrl()); }
}
