package com.indralokbright.controller;

import com.indralokbright.model.PurchaseOrder;
import com.indralokbright.model.PurchaseOrderItem;
import com.indralokbright.service.PdfService;
import com.indralokbright.service.PurchaseOrderService;
import com.indralokbright.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/purchase-order")
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final QuotationService quotationService;
    private final PdfService pdfService;

    // ---- LIST ----
    @GetMapping
    public String list(Model model) {
        model.addAttribute("purchaseOrders", purchaseOrderService.findAll());
        model.addAttribute("pageTitle", "Purchase Orders");
        return "purchaseorder/list";
    }

    // ---- NEW FORM ----
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long fromQuotation, Model model) {
        PurchaseOrder po = PurchaseOrder.builder()
                .poDate(LocalDate.now())
                .gstType("CGST_SGST")
                .cgstPercent(new BigDecimal("9"))
                .sgstPercent(new BigDecimal("9"))
                .igstPercent(BigDecimal.ZERO)
                .status("DRAFT")
                .build();

        if (fromQuotation != null) {
            try {
                var q = quotationService.findById(fromQuotation);
                po.setQuotation(q);
                po.setBuyerName(q.getBillToCompany());
                po.setBuyerGstin(q.getBillToGstin());
                po.setBuyerAddress(q.getBillToAddress());
                po.setDeliverySite(q.getShipToCompany());
                po.setDeliveryAddress(q.getShipToAddress());
                po.setDeliveryCity(q.getShipToCity());
                po.setDeliveryState(q.getShipToState());
                po.setDeliveryPincode(q.getShipToPincode());
                po.setGstType(q.getGstType());
                po.setCgstPercent(q.getCgstPercent());
                po.setSgstPercent(q.getSgstPercent());
                po.setIgstPercent(q.getIgstPercent());
                // Copy items
                List<PurchaseOrderItem> items = new ArrayList<>();
                int idx = 1;
                for (var qi : q.getItems()) {
                    items.add(PurchaseOrderItem.builder()
                            .sNo(idx++)
                            .description(qi.getDescription())
                            .quantity(qi.getQuantity())
                            .unit(qi.getUnit())
                            .rate(qi.getRate())
                            .amount(qi.getAmount())
                            .build());
                }
                po.setItems(items);
                model.addAttribute("fromQuotation", q);
            } catch (Exception e) {
                log.warn("Could not load quotation {}: {}", fromQuotation, e.getMessage());
            }
        } else {
            po.getItems().add(PurchaseOrderItem.builder().sNo(1).unit("SQFT").build());
        }

        model.addAttribute("purchaseOrder", po);
        model.addAttribute("quotations", quotationService.findAll());
        model.addAttribute("pageTitle", "New Purchase Order");
        model.addAttribute("isEdit", false);
        return "purchaseorder/form";
    }

    // ---- SAVE ----
    @PostMapping("/save")
    public String save(
            @RequestParam String vendorName,
            @RequestParam(required = false) String vendorGstin,
            @RequestParam(required = false) String vendorAddress,
            @RequestParam(required = false) String vendorPhone,
            @RequestParam(required = false) String vendorEmail,
            @RequestParam(required = false) String buyerName,
            @RequestParam(required = false) String buyerGstin,
            @RequestParam(required = false) String buyerAddress,
            @RequestParam(required = false) String deliverySite,
            @RequestParam(required = false) String deliveryAddress,
            @RequestParam(required = false) String deliveryCity,
            @RequestParam(required = false) String deliveryState,
            @RequestParam(required = false) String deliveryPincode,
            @RequestParam(required = false) String deliveryDate,
            @RequestParam String poDate,
            @RequestParam(required = false) String paymentTerms,
            @RequestParam(required = false) String termsAndConditions,
            @RequestParam String gstType,
            @RequestParam(defaultValue = "0") BigDecimal cgstPercent,
            @RequestParam(defaultValue = "0") BigDecimal sgstPercent,
            @RequestParam(defaultValue = "0") BigDecimal igstPercent,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long quotationId,
            @RequestParam(required = false) Long editId,
            @RequestParam List<String> itemDescription,
            @RequestParam List<BigDecimal> itemQuantity,
            @RequestParam List<String> itemUnit,
            @RequestParam List<BigDecimal> itemRate,
            @RequestParam(required = false) List<String> itemRemarks,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            PurchaseOrder po;
            if (editId != null) {
                po = purchaseOrderService.findById(editId);
                po.getItems().clear();
            } else {
                po = new PurchaseOrder();
            }

            po.setVendorName(vendorName);
            po.setVendorGstin(vendorGstin);
            po.setVendorAddress(vendorAddress);
            po.setVendorPhone(vendorPhone);
            po.setVendorEmail(vendorEmail);
            po.setBuyerName(buyerName);
            po.setBuyerGstin(buyerGstin);
            po.setBuyerAddress(buyerAddress);
            po.setDeliverySite(deliverySite);
            po.setDeliveryAddress(deliveryAddress);
            po.setDeliveryCity(deliveryCity);
            po.setDeliveryState(deliveryState);
            po.setDeliveryPincode(deliveryPincode);
            po.setPoDate(LocalDate.parse(poDate));
            if (deliveryDate != null && !deliveryDate.isBlank())
                po.setDeliveryDate(LocalDate.parse(deliveryDate));
            po.setPaymentTerms(paymentTerms);
            po.setTermsAndConditions(termsAndConditions);
            po.setGstType(gstType);
            po.setCgstPercent(cgstPercent);
            po.setSgstPercent(sgstPercent);
            po.setIgstPercent(igstPercent);
            po.setStatus(status);
            po.setNotes(notes);

            if (quotationId != null) {
                po.setQuotation(quotationService.findById(quotationId));
            }

			/*
			 * List<PurchaseOrderItem> items = new ArrayList<>(); for (int i = 0; i <
			 * itemDescription.size(); i++) { if (itemDescription.get(i) == null ||
			 * itemDescription.get(i).isBlank()) continue; PurchaseOrderItem item =
			 * PurchaseOrderItem.builder() .sNo(i + 1) .description(itemDescription.get(i))
			 * .quantity(itemQuantity.get(i)) .unit(itemUnit.get(i)) .rate(itemRate.get(i))
			 * .remarks(itemRemarks != null && i < itemRemarks.size() ? itemRemarks.get(i) :
			 * null) .build(); items.add(item); } po.setItems(items);
			 */
            
            
            List<PurchaseOrderItem> existingItems = po.getItems();

            existingItems.clear(); // remove old

            for (int i = 0; i < itemDescription.size(); i++) {
                if (itemDescription.get(i) == null || itemDescription.get(i).isBlank()) continue;

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .sNo(i + 1)
                        .description(itemDescription.get(i))
                        .quantity(itemQuantity.get(i))
                        .unit(itemUnit.get(i))
                        .rate(itemRate.get(i))
                        .remarks(itemRemarks != null && i < itemRemarks.size() ? itemRemarks.get(i) : null)
                        .purchaseOrder(po) // 🔥 VERY IMPORTANT
                        .build();

                existingItems.add(item);
            }

            PurchaseOrder saved = purchaseOrderService.save(po, auth.getName());
            redirectAttributes.addFlashAttribute("successMsg",
                    "Purchase Order " + saved.getPoNumber() + " saved successfully!");
            return "redirect:/purchase-order/" + saved.getId();

        } catch (Exception e) {
            log.error("Error saving PO: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMsg", "Error saving PO: " + e.getMessage());
            return "redirect:/purchase-order/new";
        }
    }

    // ---- VIEW ----
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        PurchaseOrder po = purchaseOrderService.findById(id);
        model.addAttribute("purchaseOrder", po);
        model.addAttribute("pageTitle", "Purchase Order - " + po.getPoNumber());
        return "purchaseorder/view";
    }

    // ---- EDIT FORM ----
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PurchaseOrder po = purchaseOrderService.findById(id);
        model.addAttribute("purchaseOrder", po);
        model.addAttribute("quotations", quotationService.findAll());
        model.addAttribute("pageTitle", "Edit PO - " + po.getPoNumber());
        model.addAttribute("isEdit", true);
        return "purchaseorder/form";
    }

    // ---- DELETE ----
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PurchaseOrder po = purchaseOrderService.findById(id);
            String num = po.getPoNumber();
            purchaseOrderService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "PO " + num + " deleted.");
        } catch (Exception e) {
            log.error("Error deleting PO {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/purchase-order";
    }

    // ---- STATUS UPDATE ----
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        purchaseOrderService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMsg", "Status updated to " + status);
        return "redirect:/purchase-order/" + id;
    }

    // ---- PDF DOWNLOAD ----
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            PurchaseOrder po = purchaseOrderService.findById(id);
            byte[] pdf = pdfService.generatePurchaseOrderPdf(po);
            String filename = "PO_" + po.getPoNumber() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generating PDF for PO {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PO PDF", e);
        }
    }
}
