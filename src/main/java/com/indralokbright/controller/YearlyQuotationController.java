package com.indralokbright.controller;

import com.indralokbright.model.YearlyQuotation;
import com.indralokbright.model.YearlyQuotationItem;
import com.indralokbright.service.PdfService;
import com.indralokbright.service.YearlyQuotationService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/yearly-quotation")
@RequiredArgsConstructor
@Slf4j
public class YearlyQuotationController {

    private final YearlyQuotationService service;
    private final PdfService pdfService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("quotations", service.findAll());
        model.addAttribute("pageTitle", "Yearly Quotations");
        return "yearlyquotation/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        YearlyQuotation q = YearlyQuotation.builder()
                .quotationDate(LocalDate.now())
                .gstType("CGST_SGST")
                .cgstPercent(new BigDecimal("9"))
                .sgstPercent(new BigDecimal("9"))
                .igstPercent(BigDecimal.ZERO)
                .status("DRAFT")
                // default: 3 years
                .yearsCovered((LocalDate.now().getYear() - 2) + "," +
                               (LocalDate.now().getYear() - 1) + "," +
                               LocalDate.now().getYear())
                .numberOfYears(3)
                .build();
        // one blank item
        q.getItems().add(YearlyQuotationItem.builder().sNo(1).unit("SQFT")
                .yearlyQuantities("0,0,0").marginPercent(BigDecimal.ZERO).build());
        model.addAttribute("quotation", q);
        model.addAttribute("pageTitle", "New Yearly Quotation");
        model.addAttribute("isEdit", false);
        // pass years list for column headers
        model.addAttribute("yearsList", parseYears(q.getYearsCovered()));
        return "yearlyquotation/form";
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
            @RequestParam String yearsCovered,           // e.g. "2022,2023,2024"
            @RequestParam String gstType,
            @RequestParam(defaultValue = "0") BigDecimal cgstPercent,
            @RequestParam(defaultValue = "0") BigDecimal sgstPercent,
            @RequestParam(defaultValue = "0") BigDecimal igstPercent,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long editId,
            // per-item arrays
            @RequestParam List<String>     itemDescription,
            @RequestParam List<String>     itemUnit,
            @RequestParam List<BigDecimal> itemRate,
            @RequestParam List<BigDecimal> itemMarginPercent,
            // yearly qty: each element is a comma-joined set of qtys for that item
            // sent as itemYearlyQty[0], itemYearlyQty[1], ...
            @RequestParam List<String>     itemYearlyQty,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            YearlyQuotation q = (editId != null) ? service.findById(editId) : new YearlyQuotation();
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
            q.setYearsCovered(yearsCovered.trim());
            q.setGstType(gstType);
            q.setCgstPercent(cgstPercent);
            q.setSgstPercent(sgstPercent);
            q.setIgstPercent(igstPercent);
            q.setStatus(status);
            q.setNotes(notes);

            List<YearlyQuotationItem> items = new ArrayList<>();
            for (int i = 0; i < itemDescription.size(); i++) {
                if (itemDescription.get(i) == null || itemDescription.get(i).isBlank()) continue;
                items.add(YearlyQuotationItem.builder()
                        .sNo(i + 1)
                        .description(itemDescription.get(i))
                        .unit(itemUnit.get(i))
                        .rate(itemRate.get(i))
                        .marginPercent(itemMarginPercent.get(i))
                        .yearlyQuantities(itemYearlyQty.get(i))
                        .build());
            }
            q.setItems(items);

            YearlyQuotation saved = service.save(q, auth.getName());
            ra.addFlashAttribute("successMsg", saved.getQuotationNumber() + " saved!");
            return "redirect:/yearly-quotation/" + saved.getId();
        } catch (Exception e) {
            log.error("Error saving yearly quotation: {}", e.getMessage(), e);
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
            return "redirect:/yearly-quotation/new";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        YearlyQuotation q = service.findById(id);
        model.addAttribute("quotation", q);
        model.addAttribute("yearsList", parseYears(q.getYearsCovered()));
        model.addAttribute("pageTitle", "Yearly Quotation — " + q.getQuotationNumber());
        return "yearlyquotation/view";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        YearlyQuotation q = service.findById(id);
        model.addAttribute("quotation", q);
        model.addAttribute("yearsList", parseYears(q.getYearsCovered()));
        model.addAttribute("pageTitle", "Edit — " + q.getQuotationNumber());
        model.addAttribute("isEdit", true);
        return "yearlyquotation/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            YearlyQuotation q = service.findById(id);
            service.deleteById(id);
            ra.addFlashAttribute("successMsg", q.getQuotationNumber() + " deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/yearly-quotation";
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        service.updateStatus(id, status);
        ra.addFlashAttribute("successMsg", "Status updated.");
        return "redirect:/yearly-quotation/" + id;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        YearlyQuotation q = service.findById(id);
        byte[] data = pdfService.generateYearlyQuotationPdf(q);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"YearlyQuotation_" + q.getQuotationNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    private List<String> parseYears(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim)
                .filter(s -> !s.isBlank()).collect(Collectors.toList());
    }
}
