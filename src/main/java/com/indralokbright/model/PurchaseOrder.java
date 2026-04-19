package com.indralokbright.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String poNumber;

    @Column(nullable = false)
    private LocalDate poDate;

    // Linked Quotation (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quotation quotation;

    // Vendor / Supplier
    @Column(nullable = false, length = 200)
    private String vendorName;

    @Column(length = 500)
    private String vendorAddress;

    @Column(length = 20)
    private String vendorGstin;

    @Column(length = 100)
    private String vendorPhone;

    @Column(length = 100)
    private String vendorEmail;

    // Bill To (Buyer)
    @Column(length = 200)
    private String buyerName;

    @Column(length = 20)
    private String buyerGstin;

    @Column(length = 500)
    private String buyerAddress;

    // Ship To / Delivery
    @Column(length = 200)
    private String deliverySite;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(length = 100)
    private String deliveryCity;

    @Column(length = 100)
    private String deliveryState;

    @Column(length = 10)
    private String deliveryPincode;

    @Column
    private LocalDate deliveryDate;

    // Terms
    @Column(length = 200)
    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    // GST
    @Column(length = 10)
    @Builder.Default
    private String gstType = "CGST_SGST";

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cgstPercent = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal sgstPercent = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal igstPercent = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 20)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, ISSUED, ACKNOWLEDGED, CLOSED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sNo ASC")
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String createdBy;

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
    }
}
