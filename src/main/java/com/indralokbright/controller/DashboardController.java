package com.indralokbright.controller;

import com.indralokbright.service.MarginQuotationService;
import com.indralokbright.service.PurchaseOrderService;
import com.indralokbright.service.QuotationService;
import com.indralokbright.service.YearlyQuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final QuotationService quotationService;
    private final PurchaseOrderService purchaseOrderService;
    private final MarginQuotationService marginQuotationService;
    private final YearlyQuotationService yearlyQuotationService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication auth) {
        log.debug("Dashboard accessed by: {}", auth.getName());

        model.addAttribute("totalQuotations", quotationService.countAll());
        model.addAttribute("draftQuotations", quotationService.countByStatus("DRAFT"));
        model.addAttribute("sentQuotations", quotationService.countByStatus("SENT"));
        model.addAttribute("acceptedQuotations", quotationService.countByStatus("ACCEPTED"));

        model.addAttribute("totalPOs", purchaseOrderService.countAll());
        model.addAttribute("draftPOs", purchaseOrderService.countByStatus("DRAFT"));
        model.addAttribute("issuedPOs", purchaseOrderService.countByStatus("ISSUED"));
        model.addAttribute("closedPOs", purchaseOrderService.countByStatus("CLOSED"));

        model.addAttribute("totalMarginQuotations", marginQuotationService.countAll());
        model.addAttribute("totalYearlyQuotations", yearlyQuotationService.countAll());

        model.addAttribute("recentQuotations",
                quotationService.findAll().stream().limit(5).toList());
        model.addAttribute("recentPOs",
                purchaseOrderService.findAll().stream().limit(5).toList());
        model.addAttribute("recentMarginQuotations",
                marginQuotationService.findAll().stream().limit(3).toList());
        model.addAttribute("recentYearlyQuotations",
                yearlyQuotationService.findAll().stream().limit(3).toList());

        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard";
    }
}
