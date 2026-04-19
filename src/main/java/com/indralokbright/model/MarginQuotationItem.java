package com.indralokbright.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "margin_quotation_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginQuotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "margin_quotation_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MarginQuotation marginQuotation;

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

    // qty * rate
    @Column(precision = 15, scale = 2)
    private BigDecimal baseAmount;

    // user-defined margin %
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
