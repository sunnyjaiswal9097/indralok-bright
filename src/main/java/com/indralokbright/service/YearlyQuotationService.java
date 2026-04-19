package com.indralokbright.service;

import com.indralokbright.exception.ResourceNotFoundException;
import com.indralokbright.model.YearlyQuotation;
import com.indralokbright.model.YearlyQuotationItem;
import com.indralokbright.repository.YearlyQuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class YearlyQuotationService {

    private final YearlyQuotationRepository repo;

    public List<YearlyQuotation> findAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public YearlyQuotation findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yearly Quotation", "id", id));
    }

    public List<YearlyQuotation> search(String kw) {
        return repo.search(kw);
    }

    public YearlyQuotation save(YearlyQuotation q, String username) {
        if (q.getId() == null) {
            q.setQuotationNumber(generateNumber());
            q.setCreatedBy(username);
            log.info("Creating Yearly Quotation: {}", q.getQuotationNumber());
        } else {
            log.info("Updating Yearly Quotation: {}", q.getQuotationNumber());
        }

        // Derive number of years from yearsCovered
        if (q.getYearsCovered() != null && !q.getYearsCovered().isBlank()) {
            String[] yrArr = q.getYearsCovered().split(",");
            q.setNumberOfYears(yrArr.length);
        }

        // Process items
        if (q.getItems() != null) {
            int idx = 1;
            for (YearlyQuotationItem item : q.getItems()) {
                item.setYearlyQuotation(q);
                item.setSNo(idx++);
                computeItemAmounts(item, q.getNumberOfYears());
            }
        }
        computeTotals(q);
        return repo.save(q);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        YearlyQuotation q = findById(id);
        q.setStatus(status);
        repo.save(q);
    }

    // -------- parsing helpers exposed to controller --------

    /**
     * Parse yearly quantities CSV string into a list of BigDecimals.
     * e.g. "200000,210000,195000" → [200000, 210000, 195000]
     */
    public static List<BigDecimal> parseQuantities(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> { try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; } })
                .collect(Collectors.toList());
    }

    /**
     * Build CSV from individual yearly qty fields.
     */
    public static String buildCsv(List<String> quantities) {
        if (quantities == null) return "";
        return quantities.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(","));
    }

    // ---------- private ----------

    private void computeItemAmounts(YearlyQuotationItem item, int numYears) {
        // Calculate average from yearlyQuantities CSV
        List<BigDecimal> qtys = parseQuantities(item.getYearlyQuantities());
        BigDecimal avg = BigDecimal.ZERO;
        if (!qtys.isEmpty()) {
            BigDecimal sum = qtys.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            int divisor = numYears > 0 ? numYears : qtys.size();
            avg = sum.divide(BigDecimal.valueOf(divisor), 4, RoundingMode.HALF_UP);
        }
        item.setAverageQuantity(avg);

        BigDecimal rate = nvl(item.getRate());
        BigDecimal base = avg.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        item.setBaseAmount(base);

        BigDecimal mPct = nvl(item.getMarginPercent());
        BigDecimal mAmt = base.multiply(mPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        item.setMarginAmount(mAmt);
        item.setFinalAmount(base.add(mAmt));
    }

    private void computeTotals(YearlyQuotation q) {
        BigDecimal beforeMargin = BigDecimal.ZERO, mTotal = BigDecimal.ZERO, afterMargin = BigDecimal.ZERO;
        if (q.getItems() != null) {
            for (YearlyQuotationItem i : q.getItems()) {
                beforeMargin = beforeMargin.add(nvl(i.getBaseAmount()));
                mTotal       = mTotal.add(nvl(i.getMarginAmount()));
                afterMargin  = afterMargin.add(nvl(i.getFinalAmount()));
            }
        }
        q.setSubtotalBeforeMargin(beforeMargin);
        q.setTotalMarginAmount(mTotal);
        q.setSubtotalAfterMargin(afterMargin);

        if ("IGST".equals(q.getGstType())) {
            BigDecimal igst = afterMargin.multiply(nvl(q.getIgstPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            q.setIgstAmount(igst);
            q.setCgstAmount(BigDecimal.ZERO);
            q.setSgstAmount(BigDecimal.ZERO);
            q.setGrandTotal(afterMargin.add(igst));
        } else if ("NONE".equals(q.getGstType())) {
            q.setCgstAmount(BigDecimal.ZERO);
            q.setSgstAmount(BigDecimal.ZERO);
            q.setIgstAmount(BigDecimal.ZERO);
            q.setGrandTotal(afterMargin);
        } else {
            BigDecimal cgst = afterMargin.multiply(nvl(q.getCgstPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal sgst = afterMargin.multiply(nvl(q.getSgstPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            q.setCgstAmount(cgst);
            q.setSgstAmount(sgst);
            q.setIgstAmount(BigDecimal.ZERO);
            q.setGrandTotal(afterMargin.add(cgst).add(sgst));
        }
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String generateNumber() {
        String yr = String.valueOf(LocalDate.now().getYear());
        Integer max = repo.findMaxSeq(yr);
        return String.format("IB-YQ-%s-%04d", yr, (max == null ? 0 : max) + 1);
    }

    public long countAll()              { return repo.count(); }
    public long countByStatus(String s) { return repo.countByStatus(s); }
}
