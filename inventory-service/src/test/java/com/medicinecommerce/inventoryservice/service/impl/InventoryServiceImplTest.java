package com.medicinecommerce.inventoryservice.service.impl;

import com.medicinecommerce.inventoryservice.client.ProductClient;
import com.medicinecommerce.inventoryservice.dto.QuantityRequest;
import com.medicinecommerce.inventoryservice.entity.Inventory;
import com.medicinecommerce.inventoryservice.exception.InsufficientInventoryException;
import com.medicinecommerce.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
    @Mock private InventoryRepository repository;
    @Mock private ProductClient productClient;
    @InjectMocks private InventoryServiceImpl inventoryService;

    @Test
    void reservesAvailableInventory() {
        Inventory inventory = inventory(1L, 10, 2);
        when(repository.findWithLockByProductId(1L)).thenReturn(Optional.of(inventory));
        when(repository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inventoryService.reserve(1L, new QuantityRequest(3));

        assertThat(response.availableQuantity()).isEqualTo(7);
        assertThat(response.reservedQuantity()).isEqualTo(5);
        verify(repository).save(inventory);
    }

    @Test
    void rejectsReservationWhenAvailableInventoryIsInsufficient() {
        Inventory inventory = inventory(1L, 2, 0);
        when(repository.findWithLockByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserve(1L, new QuantityRequest(3)))
                .isInstanceOf(InsufficientInventoryException.class);

        assertThat(inventory.getAvailableQuantity()).isEqualTo(2);
        assertThat(inventory.getReservedQuantity()).isZero();
        verify(repository, never()).save(any());
    }

    @Test
    void releasesReservedInventory() {
        Inventory inventory = inventory(1L, 4, 6);
        when(repository.findWithLockByProductId(1L)).thenReturn(Optional.of(inventory));
        when(repository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inventoryService.release(1L, new QuantityRequest(2));

        assertThat(response.availableQuantity()).isEqualTo(6);
        assertThat(response.reservedQuantity()).isEqualTo(4);
    }

    private Inventory inventory(Long productId, int available, int reserved) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(available);
        inventory.setReservedQuantity(reserved);
        return inventory;
    }
}
