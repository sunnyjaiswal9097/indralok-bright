package com.indralokbright.controller;

import com.indralokbright.exception.ResourceNotFoundException;
import com.indralokbright.model.Quotation;
import com.indralokbright.model.QuotationItem;
import com.indralokbright.service.PdfService;
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
@RequestMapping("/quotation")
@RequiredArgsConstructor
@Slf4j
public class QuotationController {

    private final QuotationService quotationService;
    private final PdfService pdfService;

    // ---- LIST ----
    @GetMapping
    public String list(Model model) {
        model.addAttribute("quotations", quotationService.findAll());
        model.addAttribute("pageTitle", "Quotations");
        return "quotation/list";
    }

    // ---- NEW FORM ----
    @GetMapping("/new")
    public String newForm(Model model) {
        Quotation q = Quotation.builder()
                .quotationDate(LocalDate.now())
                .gstType("CGST_SGST")
                .cgstPercent(new BigDecimal("9"))
                .sgstPercent(new BigDecimal("9"))
                .igstPercent(BigDecimal.ZERO)
                .status("DRAFT")
                .build();
        q.getItems().add(QuotationItem.builder().sNo(1).unit("SQFT").build());
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "New Quotation");
        model.addAttribute("isEdit", false);
        return "quotation/form";
    }

    // ---- SAVE (Create/Update) ----
	/*
	 * @PostMapping("/save") public String save(
	 * 
	 * @RequestParam String billToCompany,
	 * 
	 * @RequestParam(required = false) String billToGstin,
	 * 
	 * @RequestParam(required = false) String billToAddress,
	 * 
	 * @RequestParam(required = false) String shipToCompany,
	 * 
	 * @RequestParam(required = false) String shipToSiteInfo,
	 * 
	 * @RequestParam(required = false) String shipToAddress,
	 * 
	 * @RequestParam(required = false) String shipToCity,
	 * 
	 * @RequestParam(required = false) String shipToState,
	 * 
	 * @RequestParam(required = false) String shipToPincode,
	 * 
	 * @RequestParam String quotationDate,
	 * 
	 * @RequestParam(required = false) String validityPeriod,
	 * 
	 * @RequestParam String gstType,
	 * 
	 * @RequestParam(defaultValue = "0") BigDecimal cgstPercent,
	 * 
	 * @RequestParam(defaultValue = "0") BigDecimal sgstPercent,
	 * 
	 * @RequestParam(defaultValue = "0") BigDecimal igstPercent,
	 * 
	 * @RequestParam String status,
	 * 
	 * @RequestParam(required = false) String notes,
	 * 
	 * @RequestParam(required = false) Long editId, // Items
	 * 
	 * @RequestParam List<String> itemDescription,
	 * 
	 * @RequestParam List<BigDecimal> itemQuantity,
	 * 
	 * @RequestParam List<String> itemUnit,
	 * 
	 * @RequestParam List<BigDecimal> itemRate, Authentication auth,
	 * RedirectAttributes redirectAttributes) {
	 * 
	 * try { Quotation q; if (editId != null) { q =
	 * quotationService.findById(editId); q.getItems().clear(); } else { q = new
	 * Quotation(); }
	 * 
	 * q.setBillToCompany(billToCompany); q.setBillToGstin(billToGstin);
	 * q.setBillToAddress(billToAddress); q.setShipToCompany(shipToCompany);
	 * q.setShipToSiteInfo(shipToSiteInfo); q.setShipToAddress(shipToAddress);
	 * q.setShipToCity(shipToCity); q.setShipToState(shipToState);
	 * q.setShipToPincode(shipToPincode);
	 * q.setQuotationDate(LocalDate.parse(quotationDate));
	 * q.setValidityPeriod(validityPeriod); q.setGstType(gstType);
	 * q.setCgstPercent(cgstPercent); q.setSgstPercent(sgstPercent);
	 * q.setIgstPercent(igstPercent); q.setStatus(status); q.setNotes(notes);
	 * 
	 * List<QuotationItem> items = new ArrayList<>(); for (int i = 0; i <
	 * itemDescription.size(); i++) { if (itemDescription.get(i) == null ||
	 * itemDescription.get(i).isBlank()) continue; QuotationItem item =
	 * QuotationItem.builder() .sNo(i + 1) .description(itemDescription.get(i))
	 * .quantity(itemQuantity.get(i)) .unit(itemUnit.get(i)) .rate(itemRate.get(i))
	 * .build(); items.add(item); } q.setItems(items);
	 * 
	 * Quotation saved = quotationService.save(q, auth.getName());
	 * redirectAttributes.addFlashAttribute("successMsg", "Quotation " +
	 * saved.getQuotationNumber() + " saved successfully!"); return
	 * "redirect:/quotation/" + saved.getId();
	 * 
	 * } catch (Exception e) { log.error("Error saving quotation: {}",
	 * e.getMessage(), e); redirectAttributes.addFlashAttribute("errorMsg",
	 * "Error saving quotation: " + e.getMessage()); return
	 * "redirect:/quotation/new"; } }
	 */
    
    
    
	/*
	 * @PostMapping("/save") public String save(
	 * 
	 * @RequestParam String billToCompany,
	 * 
	 * @RequestParam(required = false) String billToGstin,
	 * 
	 * @RequestParam(required = false) String billToAddress,
	 * 
	 * @RequestParam(required = false) String shipToCompany,
	 * 
	 * @RequestParam(required = false) String shipToSiteInfo,
	 * 
	 * @RequestParam(required = false) String shipToAddress,
	 * 
	 * @RequestParam(required = false) String shipToCity,
	 * 
	 * @RequestParam(required = false) String shipToState,
	 * 
	 * @RequestParam(required = false) String shipToPincode,
	 * 
	 * @RequestParam String quotationDate,
	 * 
	 * @RequestParam(required = false) String validityPeriod,
	 * 
	 * @RequestParam String gstType,
	 * 
	 * @RequestParam(defaultValue = "0") BigDecimal cgstPercent,
	 * 
	 * @RequestParam(defaultValue = "0") BigDecimal sgstPercent,
	 * 
	 * @RequestParam(defaultValue = "0") BigDecimal igstPercent,
	 * 
	 * @RequestParam String status,
	 * 
	 * @RequestParam(required = false) String notes,
	 * 
	 * @RequestParam(required = false, defaultValue = "QUOTATION") String
	 * quotationTitle,
	 * 
	 * @RequestParam(required = false) Long editId,
	 * 
	 * // Items
	 * 
	 * @RequestParam List<String> itemDescription,
	 * 
	 * @RequestParam List<BigDecimal> itemQuantity,
	 * 
	 * @RequestParam List<String> itemUnit,
	 * 
	 * @RequestParam List<BigDecimal> itemRate,
	 * 
	 * Authentication auth, RedirectAttributes redirectAttributes) {
	 * 
	 * try { Quotation q;
	 * 
	 * // 🔁 EDIT CASE if (editId != null) { q = quotationService.findById(editId);
	 * 
	 * if (q == null) { redirectAttributes.addFlashAttribute("errorMsg",
	 * "Quotation not found!"); return "redirect:/quotation/list"; }
	 * 
	 * // IMPORTANT: clear existing items (do not replace list)
	 * q.getItems().clear();
	 * 
	 * } else { // 🆕 NEW CASE q = new Quotation(); }
	 * 
	 * // ============================= // 🧾 SET BASIC DETAILS //
	 * ============================= q.setBillToCompany(billToCompany);
	 * q.setBillToGstin(billToGstin); q.setBillToAddress(billToAddress);
	 * 
	 * q.setShipToCompany(shipToCompany); q.setShipToSiteInfo(shipToSiteInfo);
	 * q.setShipToAddress(shipToAddress); q.setShipToCity(shipToCity);
	 * q.setShipToState(shipToState); q.setShipToPincode(shipToPincode);
	 * 
	 * q.setQuotationDate(LocalDate.parse(quotationDate));
	 * q.setValidityPeriod(validityPeriod);
	 * 
	 * q.setGstType(gstType); q.setCgstPercent(cgstPercent);
	 * q.setSgstPercent(sgstPercent); q.setIgstPercent(igstPercent);
	 * 
	 * q.setStatus(status); q.setNotes(notes); q.setQuotationTitle(quotationTitle);
	 * 
	 * // ============================= // 📦 HANDLE ITEMS (FIXED LOGIC) //
	 * ============================= for (int i = 0; i < itemDescription.size();
	 * i++) {
	 * 
	 * if (itemDescription.get(i) == null || itemDescription.get(i).isBlank()) {
	 * continue; }
	 * 
	 * QuotationItem item = QuotationItem.builder() .sNo(i + 1)
	 * .description(itemDescription.get(i)) .quantity(itemQuantity.get(i))
	 * .unit(itemUnit.get(i)) .rate(itemRate.get(i)) .build();
	 * 
	 * // VERY IMPORTANT (maintain relation) item.setQuotation(q);
	 * 
	 * // Add to existing list (DO NOT replace list) q.getItems().add(item); }
	 * 
	 * // ============================= // 💾 SAVE // =============================
	 * Quotation saved = quotationService.save(q, auth.getName());
	 * 
	 * redirectAttributes.addFlashAttribute("successMsg", "Quotation " +
	 * saved.getQuotationNumber() + " saved successfully!");
	 * 
	 * return "redirect:/quotation/" + saved.getId();
	 * 
	 * } catch (Exception e) { log.error("Error saving quotation: {}",
	 * e.getMessage(), e);
	 * 
	 * redirectAttributes.addFlashAttribute("errorMsg", "Error saving quotation: " +
	 * e.getMessage());
	 * 
	 * return "redirect:/quotation/new"; } }
	 */  
    
    
    @PostMapping("/save")
    public String save(
            @RequestParam String billToCompany,
            @RequestParam(required = false) String billToGstin,
            @RequestParam(required = false) String billToAddress,
            @RequestParam(required = false) String shipToCompany,
            @RequestParam(required = false) String shipToSiteInfo,
            @RequestParam(required = false) String shipToAddress,
            @RequestParam(required = false) String shipToCity,
            @RequestParam(required = false) String shipToState,
            @RequestParam(required = false) String shipToPincode,
            @RequestParam String quotationDate,
            @RequestParam(required = false) String validityPeriod,
            @RequestParam String gstType,
            @RequestParam(defaultValue = "0") BigDecimal cgstPercent,
            @RequestParam(defaultValue = "0") BigDecimal sgstPercent,
            @RequestParam(defaultValue = "0") BigDecimal igstPercent,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false, defaultValue = "QUOTATION") String quotationTitle,
            @RequestParam(required = false) Long editId,

            // Items
            @RequestParam(required = false) List<String> itemDescription,
            @RequestParam(required = false) List<BigDecimal> itemQuantity,
            @RequestParam(required = false) List<String> itemUnit,
            @RequestParam(required = false) List<BigDecimal> itemRate,

            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Quotation q;

            // 🔁 EDIT CASE
            if (editId != null) {
                q = quotationService.findById(editId);

                if (q == null) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Quotation not found!");
                    return "redirect:/quotation/list";
                }

                // Clear existing items
                q.getItems().clear();

            } else {
                // 🆕 NEW CASE
                q = new Quotation();
            }

            // =============================
            // 🧾 SET BASIC DETAILS
            // =============================
            q.setBillToCompany(billToCompany);
            q.setBillToGstin(billToGstin);
            q.setBillToAddress(billToAddress);

            q.setShipToCompany(shipToCompany);
            q.setShipToSiteInfo(shipToSiteInfo);
            q.setShipToAddress(shipToAddress);
            q.setShipToCity(shipToCity);
            q.setShipToState(shipToState);
            q.setShipToPincode(shipToPincode);

            q.setQuotationDate(LocalDate.parse(quotationDate));
            q.setValidityPeriod(validityPeriod);

            q.setGstType(gstType);
            q.setCgstPercent(cgstPercent);
            q.setSgstPercent(sgstPercent);
            q.setIgstPercent(igstPercent);

            q.setStatus(status);
            q.setNotes(notes);
            q.setQuotationTitle(quotationTitle);

            // =============================
            // 📦 HANDLE ITEMS (SAFE LOGIC)
            // =============================

            // Guard: if no items submitted at all
            if (itemDescription != null && !itemDescription.isEmpty()) {

                // Use MINIMUM size across all lists to prevent IndexOutOfBoundsException
                // This handles cases where special characters in textarea cause parsing differences
                int itemCount = itemDescription.size();
                if (itemQuantity != null) itemCount = Math.min(itemCount, itemQuantity.size());
                if (itemUnit     != null) itemCount = Math.min(itemCount, itemUnit.size());
                if (itemRate     != null) itemCount = Math.min(itemCount, itemRate.size());

                int sNoCounter = 1;

                for (int i = 0; i < itemCount; i++) {

                    String desc = itemDescription.get(i);

                    // Skip blank rows
                    if (desc == null || desc.isBlank()) continue;

                    // Safe get with fallback
                    BigDecimal qty  = (itemQuantity != null && i < itemQuantity.size())
                                      ? itemQuantity.get(i) : BigDecimal.ZERO;
                    BigDecimal rate = (itemRate != null && i < itemRate.size())
                                      ? itemRate.get(i) : BigDecimal.ZERO;
                    String unit     = (itemUnit != null && i < itemUnit.size())
                                      ? itemUnit.get(i) : "NOS";

                    // Always calculate amount server-side — never trust readonly client field
                    BigDecimal amount = qty.multiply(rate);

                    QuotationItem item = QuotationItem.builder()
                            .sNo(sNoCounter++)
                            .description(desc)
                            .quantity(qty)
                            .unit(unit)
                            .rate(rate)
                            .amount(amount)
                            .build();

                    // Maintain relation
                    item.setQuotation(q);
                    q.getItems().add(item);
                }
            }

            // =============================
            // 💾 SAVE
            // =============================
            Quotation saved = quotationService.save(q, auth.getName());

            redirectAttributes.addFlashAttribute("successMsg",
                    "Quotation " + saved.getQuotationNumber() + " saved successfully!");

            return "redirect:/quotation/" + saved.getId();

        } catch (Exception e) {
            log.error("Error saving quotation: {}", e.getMessage(), e);

            redirectAttributes.addFlashAttribute("errorMsg",
                    "Error saving quotation: " + e.getMessage());

            return "redirect:/quotation/new";
        }
    }


    // ---- VIEW ----
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Quotation q = quotationService.findById(id);
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "Quotation - " + q.getQuotationNumber());
        return "quotation/view";
    }

    // ---- EDIT FORM ----
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Quotation q = quotationService.findById(id);
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "Edit Quotation - " + q.getQuotationNumber());
        model.addAttribute("isEdit", true);
        return "quotation/form";
    }

    // ---- DELETE ----
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Quotation q = quotationService.findById(id);
            String num = q.getQuotationNumber();
            quotationService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "Quotation " + num + " deleted.");
        } catch (Exception e) {
            log.error("Error deleting quotation {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "Error deleting: " + e.getMessage());
        }
        return "redirect:/quotation";
    }

    // ---- STATUS UPDATE ----
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        quotationService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMsg", "Status updated to " + status);
        return "redirect:/quotation/" + id;
    }

    // ---- PDF DOWNLOAD ----
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            Quotation q = quotationService.findById(id);
            byte[] pdf = pdfService.generateQuotationPdf(q);
            String filename = q.getQuotationTitle() +"_" + q.getQuotationNumber() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generating PDF for quotation {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
