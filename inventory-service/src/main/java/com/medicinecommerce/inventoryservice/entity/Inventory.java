package com.medicinecommerce.inventoryservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventories", uniqueConstraints = @UniqueConstraint(name = "uk_inventory_product_id", columnNames = "product_id"))
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;
    @Column(nullable = false)
    private Integer availableQuantity;
    @Column(nullable = false)
    private Integer reservedQuantity;
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
}
