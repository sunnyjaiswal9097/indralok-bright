package com.indralokbright.service;

import com.indralokbright.exception.ResourceNotFoundException;
import com.indralokbright.model.MarginQuotation;
import com.indralokbright.model.MarginQuotationItem;
import com.indralokbright.repository.MarginQuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarginQuotationService {

    private final MarginQuotationRepository repo;

    public List<MarginQuotation> findAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public MarginQuotation findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Margin Quotation", "id", id));
    }

    public List<MarginQuotation> search(String kw) {
        return repo.search(kw);
    }

    public MarginQuotation save(MarginQuotation q, String username) {
        if (q.getId() == null) {
            q.setQuotationNumber(generateNumber());
            q.setCreatedBy(username);
            log.info("Creating Margin Quotation: {}", q.getQuotationNumber());
        } else {
            log.info("Updating Margin Quotation: {}", q.getQuotationNumber());
        }
        // link items
        if (q.getItems() != null) {
            int idx = 1;
            for (MarginQuotationItem item : q.getItems()) {
                item.setMarginQuotation(q);
                item.setSNo(idx++);
                computeItemAmounts(item);
            }
        }
        computeTotals(q);
        return repo.save(q);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        MarginQuotation q = findById(id);
        q.setStatus(status);
        repo.save(q);
    }

    // ---------- private ----------

    private void computeItemAmounts(MarginQuotationItem item) {
        BigDecimal qty  = nvl(item.getQuantity());
        BigDecimal rate = nvl(item.getRate());
        BigDecimal base = qty.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        item.setBaseAmount(base);

        BigDecimal mPct = nvl(item.getMarginPercent());
        BigDecimal mAmt = base.multiply(mPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        item.setMarginAmount(mAmt);
        item.setFinalAmount(base.add(mAmt));
    }

    private void computeTotals(MarginQuotation q) {
        BigDecimal sub = BigDecimal.ZERO, mTotal = BigDecimal.ZERO, afterMargin = BigDecimal.ZERO;
        if (q.getItems() != null) {
            for (MarginQuotationItem i : q.getItems()) {
                sub       = sub.add(nvl(i.getBaseAmount()));
                mTotal    = mTotal.add(nvl(i.getMarginAmount()));
                afterMargin = afterMargin.add(nvl(i.getFinalAmount()));
            }
        }
        q.setSubtotal(sub);
        q.setTotalMarginAmount(mTotal);
        q.setTotalAfterMargin(afterMargin);

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
        return String.format("IB-MQ-%s-%04d", yr, (max == null ? 0 : max) + 1);
    }

    public long countAll()               { return repo.count(); }
    public long countByStatus(String s)  { return repo.countByStatus(s); }
}
