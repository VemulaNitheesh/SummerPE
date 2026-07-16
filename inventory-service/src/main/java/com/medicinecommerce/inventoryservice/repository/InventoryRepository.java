package com.medicinecommerce.inventoryservice.repository;

import com.medicinecommerce.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findWithLockByProductId(Long productId);
    boolean existsByProductId(Long productId);
}
