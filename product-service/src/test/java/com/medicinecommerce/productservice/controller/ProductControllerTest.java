package com.medicinecommerce.productservice.controller;
import com.medicinecommerce.productservice.monitoring.RequestCounter;
import com.medicinecommerce.productservice.dto.ProductResponse;
import com.medicinecommerce.productservice.exception.GlobalExceptionHandler;
import com.medicinecommerce.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean
    private ProductService productService;
    @MockBean
    private RequestCounter requestCounter;

    @Test
    void createsValidProduct() throws Exception {
        when(productService.create(any())).thenReturn(new ProductResponse(1L, "Paracetamol", "Pain relief", new BigDecimal("25.50"), "Pain Relief", "Example Pharma", null));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Paracetamol","description":"Pain relief","price":25.50,"category":"Pain Relief","manufacturer":"Example Pharma"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(25.50));
    }

    @Test
    void rejectsInvalidProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","description":"","price":0,"category":"","manufacturer":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }
}
