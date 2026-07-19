package com.medicinecommerce.orderservice.service.impl;

import com.medicinecommerce.orderservice.client.InventoryClient;
import com.medicinecommerce.orderservice.client.ProductClient;
import com.medicinecommerce.orderservice.dto.OrderRequest;
import com.medicinecommerce.orderservice.dto.ProductResponse;
import com.medicinecommerce.orderservice.entity.Order;
import com.medicinecommerce.orderservice.entity.OrderStatus;
import com.medicinecommerce.orderservice.exception.DownstreamServiceUnavailableException;
import com.medicinecommerce.orderservice.exception.InsufficientInventoryException;
import com.medicinecommerce.orderservice.exception.ProductNotFoundException;
import com.medicinecommerce.orderservice.mapper.OrderMapper;
import com.medicinecommerce.orderservice.repository.OrderRepository;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRepository repository;

    @Mock
    private ProductClient productClient;

    @Mock
    private InventoryClient inventoryClient;

    @Spy
    private OrderMapper mapper = new OrderMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createsConfirmedOrderAfterProductLookupAndInventoryReservation() {
        when(productClient.getById(1L)).thenReturn(product());
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.create(request());

        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualByComparingTo("51.00");
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        verify(inventoryClient).reserve(eq(1L), argThat(quantity -> quantity.quantity().equals(2)));
        verify(repository).save(any(Order.class));
    }

    @Test
    void doesNotReserveOrSaveWhenProductDoesNotExist() {
        when(productClient.getById(1L)).thenThrow(feignStatus(404));

        assertThatThrownBy(() -> orderService.create(request())).isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(inventoryClient);
        verify(repository, never()).save(any());
    }

    @Test
    void doesNotSaveWhenInventoryIsInsufficient() {
        when(productClient.getById(1L)).thenReturn(product());
        doThrow(feignStatus(409)).when(inventoryClient).reserve(any(), any());

        assertThatThrownBy(() -> orderService.create(request())).isInstanceOf(InsufficientInventoryException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void mapsUnavailableProductServiceWithoutSavingAnOrder() {
        when(productClient.getById(1L)).thenThrow(feignStatus(503));

        assertThatThrownBy(() -> orderService.create(request())).isInstanceOf(DownstreamServiceUnavailableException.class);

        verifyNoInteractions(inventoryClient);
        verify(repository, never()).save(any());
    }

    private OrderRequest request() { return new OrderRequest("Asha Kumar", "asha@example.com", 1L, 2); }

    private ProductResponse product() {
        return new ProductResponse(1L, "Paracetamol", "Pain relief", new BigDecimal("25.50"), "Pain Relief", "Example Pharma", null);
    }

    private FeignException feignStatus(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://product-service/api/v1/products/1", Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(status).reason("test").request(request).headers(Map.of()).build();
        return FeignException.errorStatus("product-service", response);
    }
}
