package com.medicinecommerce.orderservice.repository;

import com.medicinecommerce.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> { }
