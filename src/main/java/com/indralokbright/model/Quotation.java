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
@Table(name = "quotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String quotationNumber;

    @Column(nullable = false)
    private LocalDate quotationDate;

    // Bill To
    @Column(nullable = false, length = 200)
    private String billToCompany;
    
    private String quotationTitle;

    @Column(length = 20)
    private String billToGstin;

    @Column(length = 500)
    private String billToAddress;

    // Ship To / Consignee
    @Column(length = 200)
    private String shipToCompany;

    @Column(length = 200)
    private String shipToSiteInfo;

    @Column(length = 500)
    private String shipToAddress;

    @Column(length = 100)
    private String shipToCity;

    @Column(length = 100)
    private String shipToState;

    @Column(length = 10)
    private String shipToPincode;

    // GST Type
    @Column(length = 10)
    @Builder.Default
    private String gstType = "CGST_SGST"; // CGST_SGST or IGST

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
    private String status = "DRAFT"; // DRAFT, SENT, ACCEPTED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 100)
    private String validityPeriod;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sNo ASC")
    @Builder.Default
    private List<QuotationItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String createdBy;

    public void addItem(QuotationItem item) {
        items.add(item);
        item.setQuotation(this);
    }

    public void removeItem(QuotationItem item) {
        items.remove(item);
        item.setQuotation(null);
    }
}
