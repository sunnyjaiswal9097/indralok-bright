package com.indralokbright.controller;

import com.indralokbright.service.PurchaseOrderService;
import com.indralokbright.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final QuotationService quotationService;
    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public String search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "all") String type,
            Model model) {

        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("pageTitle", "Search");

        if (!keyword.isBlank()) {
            log.info("Search request: keyword='{}' type='{}'", keyword, type);
            if ("quotation".equals(type) || "all".equals(type)) {
                model.addAttribute("quotations", quotationService.search(keyword));
            }
            if ("po".equals(type) || "all".equals(type)) {
                model.addAttribute("purchaseOrders", purchaseOrderService.search(keyword));
            }
        }

        return "search/search";
    }
}
