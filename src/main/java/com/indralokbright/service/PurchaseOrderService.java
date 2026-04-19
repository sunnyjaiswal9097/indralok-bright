package com.indralokbright.service;

import com.indralokbright.exception.ResourceNotFoundException;
import com.indralokbright.model.PurchaseOrder;
import com.indralokbright.model.PurchaseOrderItem;
import com.indralokbright.repository.PurchaseOrderRepository;
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
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", "id", id));
    }

    public PurchaseOrder findByPoNumber(String poNumber) {
        return purchaseOrderRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", "PO number", poNumber));
    }

    public List<PurchaseOrder> search(String keyword) {
        log.debug("Searching POs with keyword: {}", keyword);
        return purchaseOrderRepository.searchPurchaseOrders(keyword);
    }

    public PurchaseOrder save(PurchaseOrder po, String username) {
        if (po.getId() == null) {
            po.setPoNumber(generatePoNumber());
            po.setCreatedBy(username);
            log.info("Creating new PO: {}", po.getPoNumber());
        } else {
            log.info("Updating PO: {}", po.getPoNumber());
        }
        calculateTotals(po);
        if (po.getItems() != null) {
            for (int i = 0; i < po.getItems().size(); i++) {
                PurchaseOrderItem item = po.getItems().get(i);
                item.setPurchaseOrder(po);
                item.setSNo(i + 1);
                if (item.getQuantity() != null && item.getRate() != null) {
                    item.setAmount(item.getQuantity().multiply(item.getRate()).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        return purchaseOrderRepository.save(po);
    }

    public void deleteById(Long id) {
        PurchaseOrder po = findById(id);
        log.info("Deleting PO: {}", po.getPoNumber());
        purchaseOrderRepository.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        PurchaseOrder po = findById(id);
        po.setStatus(status);
        purchaseOrderRepository.save(po);
        log.info("PO {} status updated to {}", po.getPoNumber(), status);
    }

    private void calculateTotals(PurchaseOrder po) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                if (item.getQuantity() != null && item.getRate() != null) {
                    BigDecimal amt = item.getQuantity().multiply(item.getRate()).setScale(2, RoundingMode.HALF_UP);
                    item.setAmount(amt);
                    subtotal = subtotal.add(amt);
                }
            }
        }
        po.setSubtotal(subtotal);

        if ("IGST".equals(po.getGstType())) {
            BigDecimal igstAmt = subtotal.multiply(po.getIgstPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            po.setIgstAmount(igstAmt);
            po.setCgstAmount(BigDecimal.ZERO);
            po.setSgstAmount(BigDecimal.ZERO);
            po.setTotalAmount(subtotal.add(igstAmt));
        } else {
            BigDecimal cgstAmt = subtotal.multiply(po.getCgstPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal sgstAmt = subtotal.multiply(po.getSgstPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            po.setCgstAmount(cgstAmt);
            po.setSgstAmount(sgstAmt);
            po.setIgstAmount(BigDecimal.ZERO);
            po.setTotalAmount(subtotal.add(cgstAmt).add(sgstAmt));
        }
    }

    private String generatePoNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        Integer maxSeq = purchaseOrderRepository.findMaxSequenceForYear(year);
        int nextSeq = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("PO-%s-%04d", year, nextSeq);
    }

    public long countAll() { return purchaseOrderRepository.count(); }
    public long countByStatus(String status) { return purchaseOrderRepository.countByStatus(status); }
}
