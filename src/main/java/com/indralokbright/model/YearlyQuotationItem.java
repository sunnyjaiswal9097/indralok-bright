package com.indralokbright.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One line item in a Yearly Quotation.
 * Yearly quantities are stored as a JSON/CSV string for flexibility.
 * The service layer parses them and computes the average.
 */
@Entity
@Table(name = "yearly_quotation_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyQuotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yearly_quotation_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private YearlyQuotation yearlyQuotation;

    @Column(nullable = false)
    private Integer sNo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 30)
    private String unit;

    /**
     * Comma-separated yearly quantities: "200000,210000,195000"
     * Aligned with yearsCovered in parent: "2022,2023,2024"
     */
    @Column(columnDefinition = "TEXT")
    private String yearlyQuantities;

    // Average qty calculated from all years
    @Column(precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal averageQuantity = BigDecimal.ZERO;

    // Rate per unit (user defined)
    @Column(precision = 15, scale = 2)
    private BigDecimal rate;

    // averageQuantity * rate
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal baseAmount = BigDecimal.ZERO;

    // Margin % (user defined per item)
    @Column(precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal marginPercent = BigDecimal.ZERO;

    // baseAmount * marginPercent / 100
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal marginAmount = BigDecimal.ZERO;

    // baseAmount + marginAmount
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;
}
