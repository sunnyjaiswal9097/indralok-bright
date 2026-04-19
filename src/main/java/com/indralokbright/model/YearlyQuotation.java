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

/**
 * Yearly Analysis Quotation:
 * - Multiple work items, each with yearly quantity data
 * - System averages the quantities across years
 * - User applies a margin % on top of the rate
 * - GST freely defined
 * - Grand total computed
 */
@Entity
@Table(name = "yearly_quotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String quotationNumber;

    @Column(nullable = false)
    private LocalDate quotationDate;

    @Column(length = 200, nullable = false)
    private String billToCompany;

    @Column(length = 20)
    private String billToGstin;

    @Column(length = 500)
    private String billToAddress;

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

    @Column(length = 100)
    private String validityPeriod;

    // How many years of data the user has entered (derived)
    @Column
    @Builder.Default
    private Integer numberOfYears = 0;

    // The years covered (e.g. "2022, 2023, 2024")
    @Column(length = 200)
    private String yearsCovered;

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

    // Totals
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotalBeforeMargin = BigDecimal.ZERO;  // avg qty * rate

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalMarginAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotalAfterMargin = BigDecimal.ZERO;

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
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Each work item (e.g. "Ceiling Paint Work", "PT Marking", "Beam Drops")
    @OneToMany(mappedBy = "yearlyQuotation", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sNo ASC")
    @Builder.Default
    private List<YearlyQuotationItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String createdBy;
}
