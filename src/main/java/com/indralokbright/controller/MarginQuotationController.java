package com.indralokbright.controller;

import com.indralokbright.model.MarginQuotation;
import com.indralokbright.model.MarginQuotationItem;
import com.indralokbright.service.MarginQuotationService;
import com.indralokbright.service.PdfService;
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
@RequestMapping("/margin-quotation")
@RequiredArgsConstructor
@Slf4j
public class MarginQuotationController {

    private final MarginQuotationService service;
    private final PdfService pdfService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("quotations", service.findAll());
        model.addAttribute("pageTitle", "Margin Quotations");
        return "marginquotation/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        MarginQuotation q = MarginQuotation.builder()
                .quotationDate(LocalDate.now())
                .gstType("CGST_SGST")
                .cgstPercent(new BigDecimal("9"))
                .sgstPercent(new BigDecimal("9"))
                .igstPercent(BigDecimal.ZERO)
                .status("DRAFT")
                .build();
        q.getItems().add(MarginQuotationItem.builder().sNo(1).unit("SQFT").marginPercent(BigDecimal.ZERO).build());
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "New Margin Quotation");
        model.addAttribute("isEdit", false);
        return "marginquotation/form";
    }

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
            @RequestParam(required = false) Long editId,
            @RequestParam List<String> itemDescription,
            @RequestParam List<BigDecimal> itemQuantity,
            @RequestParam List<String> itemUnit,
            @RequestParam List<BigDecimal> itemRate,
            @RequestParam List<BigDecimal> itemMarginPercent,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            MarginQuotation q = (editId != null) ? service.findById(editId) : new MarginQuotation();
            if (editId != null) q.getItems().clear();

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

            List<MarginQuotationItem> items = new ArrayList<>();
            for (int i = 0; i < itemDescription.size(); i++) {
                if (itemDescription.get(i) == null || itemDescription.get(i).isBlank()) continue;
                items.add(MarginQuotationItem.builder()
                        .sNo(i + 1)
                        .description(itemDescription.get(i))
                        .quantity(itemQuantity.get(i))
                        .unit(itemUnit.get(i))
                        .rate(itemRate.get(i))
                        .marginPercent(itemMarginPercent.get(i))
                        .build());
            }
            q.setItems(items);

            MarginQuotation saved = service.save(q, auth.getName());
            ra.addFlashAttribute("successMsg", saved.getQuotationNumber() + " saved!");
            return "redirect:/margin-quotation/" + saved.getId();
        } catch (Exception e) {
            log.error("Error saving margin quotation: {}", e.getMessage(), e);
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
            return "redirect:/margin-quotation/new";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        MarginQuotation q = service.findById(id);
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "Margin Quotation — " + q.getQuotationNumber());
        return "marginquotation/view";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        MarginQuotation q = service.findById(id);
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "Edit — " + q.getQuotationNumber());
        model.addAttribute("isEdit", true);
        return "marginquotation/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            MarginQuotation q = service.findById(id);
            service.deleteById(id);
            ra.addFlashAttribute("successMsg", q.getQuotationNumber() + " deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/margin-quotation";
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        service.updateStatus(id, status);
        ra.addFlashAttribute("successMsg", "Status updated.");
        return "redirect:/margin-quotation/" + id;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        MarginQuotation q = service.findById(id);
        byte[] data = pdfService.generateMarginQuotationPdf(q);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"MarginQuotation_" + q.getQuotationNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
