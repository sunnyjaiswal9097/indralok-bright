package com.indralokbright.service;

import com.indralokbright.exception.ResourceNotFoundException;
import com.indralokbright.model.Quotation;
import com.indralokbright.model.QuotationItem;
import com.indralokbright.repository.QuotationRepository;
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
public class QuotationService {

    private final QuotationRepository quotationRepository;

    public List<Quotation> findAll() {
        return quotationRepository.findAllByOrderByCreatedAtDesc();
    }

    public Quotation findById(Long id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", id));
    }

    public Quotation findByQuotationNumber(String number) {
        return quotationRepository.findByQuotationNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "number", number));
    }

    public List<Quotation> search(String keyword) {
        log.debug("Searching quotations with keyword: {}", keyword);
        return quotationRepository.searchQuotations(keyword);
    }

    public Quotation save(Quotation quotation, String username) {
        if (quotation.getId() == null) {
            quotation.setQuotationNumber(generateQuotationNumber());
            quotation.setCreatedBy(username);
            log.info("Creating new quotation: {}", quotation.getQuotationNumber());
        } else {
            log.info("Updating quotation: {}", quotation.getQuotationNumber());
        }
        calculateTotals(quotation);
        // Link items to quotation
        if (quotation.getItems() != null) {
            for (int i = 0; i < quotation.getItems().size(); i++) {
                QuotationItem item = quotation.getItems().get(i);
                item.setQuotation(quotation);
                item.setSNo(i + 1);
                if (item.getQuantity() != null && item.getRate() != null) {
                    item.setAmount(item.getQuantity().multiply(item.getRate()).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        return quotationRepository.save(quotation);
    }

    public void deleteById(Long id) {
        Quotation q = findById(id);
        log.info("Deleting quotation: {}", q.getQuotationNumber());
        quotationRepository.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        Quotation q = findById(id);
        q.setStatus(status);
        quotationRepository.save(q);
        log.info("Quotation {} status updated to {}", q.getQuotationNumber(), status);
    }

    private void calculateTotals(Quotation q) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (q.getItems() != null) {
            for (QuotationItem item : q.getItems()) {
                if (item.getQuantity() != null && item.getRate() != null) {
                    BigDecimal amt = item.getQuantity().multiply(item.getRate()).setScale(2, RoundingMode.HALF_UP);
                    item.setAmount(amt);
                    subtotal = subtotal.add(amt);
                }
            }
        }
        q.setSubtotal(subtotal);

        if ("IGST".equals(q.getGstType())) {
            BigDecimal igstAmt = subtotal.multiply(q.getIgstPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            q.setIgstAmount(igstAmt);
            q.setCgstAmount(BigDecimal.ZERO);
            q.setSgstAmount(BigDecimal.ZERO);
            q.setTotalAmount(subtotal.add(igstAmt));
        } else {
            BigDecimal cgstAmt = subtotal.multiply(q.getCgstPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal sgstAmt = subtotal.multiply(q.getSgstPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            q.setCgstAmount(cgstAmt);
            q.setSgstAmount(sgstAmt);
            q.setIgstAmount(BigDecimal.ZERO);
            q.setTotalAmount(subtotal.add(cgstAmt).add(sgstAmt));
        }
    }

    private String generateQuotationNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        Integer maxSeq = quotationRepository.findMaxSequenceForYear(year);
        int nextSeq = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("IB-%s-%04d", year, nextSeq);
    }

    public long countAll() { return quotationRepository.count(); }
    public long countByStatus(String status) { return quotationRepository.countByStatus(status); }
}
