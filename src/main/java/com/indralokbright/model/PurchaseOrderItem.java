package com.indralokbright.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private Integer sNo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(length = 30)
    private String unit;

    @Column(precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 200)
    private String remarks;
}
