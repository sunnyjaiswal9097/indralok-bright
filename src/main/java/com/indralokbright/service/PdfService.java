package com.indralokbright.service;

import com.indralokbright.model.Quotation;
import com.indralokbright.model.QuotationItem;
import com.indralokbright.model.PurchaseOrder;
import com.indralokbright.model.PurchaseOrderItem;
import com.indralokbright.model.MarginQuotation;
import com.indralokbright.model.MarginQuotationItem;
import com.indralokbright.model.YearlyQuotation;
import com.indralokbright.model.YearlyQuotationItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.indralokbright.util.NumberToWordsUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final NumberToWordsUtil numberToWordsUtil;

    @Value("${app.company.name}") private String companyName;
    @Value("${app.company.gstin}") private String companyGstin;
    @Value("${app.company.address}") private String companyAddress;
    @Value("${app.company.phone}") private String companyPhone;
    @Value("${app.company.email}") private String companyEmail;
    @Value("${app.company.bank.name}") private String bankName;
    @Value("${app.company.bank.account}") private String bankAccount;
    @Value("${app.company.bank.ifsc}") private String bankIfsc;
    @Value("${app.company.bank.beneficiary}") private String bankBeneficiary;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat INR_FMT = NumberFormat.getInstance(new Locale("en", "IN"));

    // Company logo embedded as Base64 data URI for PDF rendering
    private static final String LOGO_BASE64;

    // Cancelled cheque embedded as Base64 data URI for PDF rendering
    private static final String CHEQUE_BASE64;

    static {
        INR_FMT.setMinimumFractionDigits(2);
        INR_FMT.setMaximumFractionDigits(2);

        // Load logo
        String logoUri = "";
        try (InputStream is = PdfService.class.getResourceAsStream("/static/images/logo.jpeg")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                logoUri = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
            } else {
                System.err.println("[PdfService] WARNING: /static/images/logo.jpeg not found on classpath");
            }
        } catch (Exception e) {
            System.err.println("[PdfService] WARNING: Failed to load company logo: " + e.getMessage());
        }
        LOGO_BASE64 = logoUri;

        // Load cancelled cheque
        String chequeUri = "";
        try (InputStream is = PdfService.class.getResourceAsStream("/static/images/cancelled_cheque.jpeg")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                chequeUri = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
            } else {
                System.err.println("[PdfService] WARNING: /static/images/cancelled_cheque.jpeg not found on classpath");
            }
        } catch (Exception e) {
            System.err.println("[PdfService] WARNING: Failed to load cancelled cheque: " + e.getMessage());
        }
        CHEQUE_BASE64 = chequeUri;
    }

    public byte[] generateQuotationPdf(Quotation q) {
        try {
            String html = buildQuotationHtml(q);
            return renderPdf(html);
        } catch (Exception e) {
            log.error("Error generating quotation PDF for {}: {}", q.getQuotationNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate quotation PDF", e);
        }
    }

    public byte[] generatePurchaseOrderPdf(PurchaseOrder po) {
        try {
            String html = buildPurchaseOrderHtml(po);
            return renderPdf(html);
        } catch (Exception e) {
            log.error("Error generating PO PDF for {}: {}", po.getPoNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PO PDF", e);
        }
    }

    private byte[] renderPdf(String html) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();
        return os.toByteArray();
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "0.00";
        return INR_FMT.format(val);
    }

    /** Returns the logo HTML: real image if loaded, fallback text badge otherwise */
    private String logoHtml() {
        if (LOGO_BASE64 != null && !LOGO_BASE64.isEmpty()) {
            return "<img src='" + LOGO_BASE64 + "' class='company-logo-img' alt='Logo'/>";
        }
        return "<div class='company-logo'>IB</div>";
    }

    /** Returns the cancelled cheque HTML if image is loaded */
    private String chequeHtml() {
        if (CHEQUE_BASE64 != null && !CHEQUE_BASE64.isEmpty()) {
            return "<div class='cheque-box'>"
                 + "<p class='cheque-label'><strong>Cancelled Cheque</strong></p>"
                 + "<img src='" + CHEQUE_BASE64 + "' class='cheque-img' alt='Cancelled Cheque'/>"
                 + "</div>";
        }
        return "";
    }

    private String buildQuotationHtml(Quotation q) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>");
        sb.append(getPdfStyles());
        sb.append("</style></head><body>");

        // Header
        sb.append("<div class='header'>");
        sb.append("<div class='logo-section'>");
        sb.append(logoHtml());
        sb.append("<div class='company-details'>");
        sb.append("<h1>").append(companyName).append("</h1>");
        sb.append("<p>GSTIN: ").append(companyGstin).append("</p>");
        sb.append("<p>").append(companyAddress).append("</p>");
        sb.append("<p>Phone: ").append(companyPhone).append("</p>");
        sb.append("<p>Email: ").append(companyEmail).append("</p>");
        sb.append("</div></div>");
        sb.append("<div class='doc-title'><h2>QUOTATION</h2>");
        sb.append("<p>No: <strong>").append(q.getQuotationNumber()).append("</strong></p>");
        sb.append("<p>Date: <strong>").append(q.getQuotationDate().format(DATE_FMT)).append("</strong></p>");
        if (q.getValidityPeriod() != null && !q.getValidityPeriod().isBlank()) {
            sb.append("<p>Valid Till: <strong>").append(q.getValidityPeriod()).append("</strong></p>");
        }
        sb.append("</div></div>");

        // Bill To / Ship To
        sb.append("<div class='address-section'>");
        sb.append("<div class='address-box'>");
        sb.append("<h3>Bill To</h3>");
        sb.append("<p><strong>").append(esc(q.getBillToCompany())).append("</strong></p>");
        if (q.getBillToGstin() != null) sb.append("<p>GSTIN: ").append(esc(q.getBillToGstin())).append("</p>");
        if (q.getBillToAddress() != null) sb.append("<p>").append(esc(q.getBillToAddress())).append("</p>");
        sb.append("</div>");
        sb.append("<div class='address-box'>");
        sb.append("<h3>Consignee (Ship To)</h3>");
        if (q.getShipToCompany() != null) sb.append("<p><strong>").append(esc(q.getShipToCompany())).append("</strong></p>");
        if (q.getShipToSiteInfo() != null) sb.append("<p>").append(esc(q.getShipToSiteInfo())).append("</p>");
        if (q.getShipToAddress() != null) sb.append("<p>").append(esc(q.getShipToAddress())).append("</p>");
        if (q.getShipToCity() != null) sb.append("<p>").append(esc(q.getShipToCity())).append(
                q.getShipToState() != null ? ", " + esc(q.getShipToState()) : "").append("</p>");
        if (q.getShipToPincode() != null) sb.append("<p>PIN: ").append(esc(q.getShipToPincode())).append("</p>");
        sb.append("</div></div>");

        // Items Table
        sb.append("<table class='items-table'>");
        sb.append("<thead><tr><th>S.No</th><th>Description</th><th>Qty</th><th>Unit</th><th>Rate (INR)</th><th>Amount (INR)</th></tr></thead>");
        sb.append("<tbody>");
        for (QuotationItem item : q.getItems()) {
            sb.append("<tr>");
            sb.append("<td class='center'>").append(item.getSNo()).append("</td>");
            sb.append("<td class='center'>").append(esc(item.getDescription())).append("</td>");
            sb.append("<td class='center'>").append(fmt(item.getQuantity())).append("</td>");
            sb.append("<td class='center'>").append(esc(item.getUnit())).append("</td>");
            sb.append("<td class='center'>").append(fmt(item.getRate())).append("</td>");
            sb.append("<td class='center'>").append(fmt(item.getAmount())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");

        // Totals Table
        sb.append("<table class='totals-table'>");
        sb.append("<tr><td>Sub Total</td><td>&#8377; ").append(fmt(q.getSubtotal())).append("</td></tr>");
        if ("IGST".equals(q.getGstType())) {
            sb.append("<tr><td>IGST @ ").append(fmt(q.getIgstPercent())).append("%</td><td>&#8377; ").append(fmt(q.getIgstAmount())).append("</td></tr>");
        } else {
            if (q.getCgstPercent().compareTo(BigDecimal.ZERO) > 0)
                sb.append("<tr><td>CGST @ ").append(fmt(q.getCgstPercent())).append("%</td><td>&#8377; ").append(fmt(q.getCgstAmount())).append("</td></tr>");
            if (q.getSgstPercent().compareTo(BigDecimal.ZERO) > 0)
                sb.append("<tr><td>SGST @ ").append(fmt(q.getSgstPercent())).append("%</td><td>&#8377; ").append(fmt(q.getSgstAmount())).append("</td></tr>");
        }
        sb.append("<tr class='total-row'><td><strong>Total</strong></td><td><strong>&#8377; ")
          .append(fmt(q.getTotalAmount())).append("</strong></td></tr>");
        sb.append("</table>");

        // Amount in Words
        sb.append("<div class='amount-words-bottom'>");
        sb.append("<p><strong>Amount in Words:</strong><br/>");
        sb.append(numberToWordsUtil.convert(q.getTotalAmount()));
        sb.append("</p></div>");

        // Notes
        if (q.getNotes() != null && !q.getNotes().isBlank()) {
            sb.append("<div class='notes-section'><h3>Terms &amp; Conditions</h3><p>").append(esc(q.getNotes())).append("</p></div>");
        }

        // Bank Details + Cancelled Cheque
        appendNotesAndBank(sb, null); // notes already appended above

        // Signature
		/*
		 * sb.append("<div class='signature-section'>"); sb.
		 * append("<div class='sig-box'><p>Prepared By</p><div class='sig-line'></div></div>"
		 * ); sb.append("<div class='sig-box'><p>For ").append(companyName).
		 * append("</p><div class='sig-line'></div><p>Authorised Signatory</p></div>");
		 * sb.append("</div>");
		 */

        sb.append("</body></html>");
        return sb.toString();
    }

    private String buildPurchaseOrderHtml(PurchaseOrder po) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>");
        sb.append(getPdfStyles());
        sb.append("</style></head><body>");

        // Header
        sb.append("<div class='header'>");
        sb.append("<div class='logo-section'>");
        sb.append(logoHtml());
        sb.append("<div class='company-details'>");
        sb.append("<h1>").append(companyName).append("</h1>");
        sb.append("<p>GSTIN: ").append(companyGstin).append("</p>");
        sb.append("<p>").append(companyAddress).append("</p>");
        sb.append("<p>Phone: ").append(companyPhone).append(" | Email: ").append(companyEmail).append("</p>");
        sb.append("</div></div>");
        sb.append("<div class='doc-title'><h2>PURCHASE ORDER</h2>");
        sb.append("<p>PO No: <strong>").append(po.getPoNumber()).append("</strong></p>");
        sb.append("<p>Date: <strong>").append(po.getPoDate().format(DATE_FMT)).append("</strong></p>");
        if (po.getQuotation() != null) sb.append("<p>Ref. Quotation: <strong>").append(po.getQuotation().getQuotationNumber()).append("</strong></p>");
        sb.append("</div></div>");

        // Vendor / Buyer
        sb.append("<div class='address-section'>");
        sb.append("<div class='address-box'>");
        sb.append("<h3>Vendor / Supplier</h3>");
        sb.append("<p><strong>").append(esc(po.getVendorName())).append("</strong></p>");
        if (po.getVendorGstin() != null) sb.append("<p>GSTIN: ").append(esc(po.getVendorGstin())).append("</p>");
        if (po.getVendorAddress() != null) sb.append("<p>").append(esc(po.getVendorAddress())).append("</p>");
        if (po.getVendorPhone() != null) sb.append("<p>Ph: ").append(esc(po.getVendorPhone())).append("</p>");
        sb.append("</div>");
        sb.append("<div class='address-box'>");
        sb.append("<h3>Delivery / Site Address</h3>");
        if (po.getDeliverySite() != null) sb.append("<p><strong>").append(esc(po.getDeliverySite())).append("</strong></p>");
        if (po.getDeliveryAddress() != null) sb.append("<p>").append(esc(po.getDeliveryAddress())).append("</p>");
        if (po.getDeliveryCity() != null) sb.append("<p>").append(esc(po.getDeliveryCity())).append(po.getDeliveryState() != null ? ", " + esc(po.getDeliveryState()) : "").append("</p>");
        if (po.getDeliveryPincode() != null) sb.append("<p>PIN: ").append(esc(po.getDeliveryPincode())).append("</p>");
        if (po.getDeliveryDate() != null) sb.append("<p>Expected Delivery: <strong>").append(po.getDeliveryDate().format(DATE_FMT)).append("</strong></p>");
        sb.append("</div></div>");

        // Items Table
        sb.append("<table class='items-table'>");
        sb.append("<thead><tr><th>S.No</th><th>Description</th><th>Qty</th><th>Unit</th><th>Rate (INR)</th><th>Amount (INR)</th><th>Remarks</th></tr></thead>");
        sb.append("<tbody>");
        for (PurchaseOrderItem item : po.getItems()) {
            sb.append("<tr>");
            sb.append("<td class='center'>").append(item.getSNo()).append("</td>");
            sb.append("<td class='center'>").append(esc(item.getDescription())).append("</td>");
            sb.append("<td class='center'>").append(fmt(item.getQuantity())).append("</td>");
            sb.append("<td class='center'>").append(esc(item.getUnit())).append("</td>");
            sb.append("<td class='center'>").append(fmt(item.getRate())).append("</td>");
            sb.append("<td class='center'>").append(fmt(item.getAmount())).append("</td>");
            sb.append("<td class='center'>").append(item.getRemarks() != null ? esc(item.getRemarks()) : "").append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");

        // Totals Table
        sb.append("<table class='totals-table'>");
        sb.append("<tr><td>Sub Total</td><td>&#8377; ").append(fmt(po.getSubtotal())).append("</td></tr>");
        if ("IGST".equals(po.getGstType())) {
            sb.append("<tr><td>IGST @ ").append(fmt(po.getIgstPercent())).append("%</td><td>&#8377; ").append(fmt(po.getIgstAmount())).append("</td></tr>");
        } else {
            if (po.getCgstPercent().compareTo(BigDecimal.ZERO) > 0)
                sb.append("<tr><td>CGST @ ").append(fmt(po.getCgstPercent())).append("%</td><td>&#8377; ").append(fmt(po.getCgstAmount())).append("</td></tr>");
            if (po.getSgstPercent().compareTo(BigDecimal.ZERO) > 0)
                sb.append("<tr><td>SGST @ ").append(fmt(po.getSgstPercent())).append("%</td><td>&#8377; ").append(fmt(po.getSgstAmount())).append("</td></tr>");
        }
        sb.append("<tr class='total-row'><td><strong>Total</strong></td><td><strong>&#8377; ")
          .append(fmt(po.getTotalAmount())).append("</strong></td></tr>");
        sb.append("</table>");

        // Amount in Words
        sb.append("<div class='amount-words-bottom'>");
        sb.append("<p><strong>Amount in Words:</strong><br/>");
        sb.append(numberToWordsUtil.convert(po.getTotalAmount()));
        sb.append("</p></div>");

        // Payment Terms / T&C
        if (po.getPaymentTerms() != null && !po.getPaymentTerms().isBlank()) {
            sb.append("<div class='notes-section'><h3>Payment Terms</h3><p>").append(esc(po.getPaymentTerms())).append("</p></div>");
        }
        if (po.getTermsAndConditions() != null && !po.getTermsAndConditions().isBlank()) {
            sb.append("<div class='notes-section'><h3>Terms &amp; Conditions</h3><p>").append(esc(po.getTermsAndConditions())).append("</p></div>");
        }

        // Bank Details + Cancelled Cheque
        appendNotesAndBank(sb, null);

        // Signature
        sb.append("<div class='signature-section'>");
        sb.append("<div class='sig-box'><p>Vendor Acknowledgement</p><div class='sig-line'></div></div>");
        sb.append("<div class='sig-box'><p>For ").append(companyName).append("</p><div class='sig-line'></div><p>Authorised Signatory</p></div>");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String getPdfStyles() {
        return """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: Arial, sans-serif; font-size: 11px; color: #222; padding: 20px; }
            .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 3px solid #1a237e; padding-bottom: 12px; margin-bottom: 12px; }
            .logo-section { display: flex; align-items: flex-start; gap: 12px; }
            .company-logo-img { width: 60px; height: 60px; object-fit: contain; }
            .company-logo { width: 55px; height: 55px; background: linear-gradient(135deg, #1a237e, #f57f17); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px; font-weight: bold; }
            .company-details h1 { font-size: 16px; color: #1a237e; margin-bottom: 3px; }
            .company-details p { font-size: 9px; color: #555; margin-bottom: 1px; }
            .doc-title { text-align: right; }
            .doc-title h2 { font-size: 18px; color: #1a237e; letter-spacing: 2px; }
            .doc-title p { font-size: 10px; margin-top: 3px; }
            .address-section { display: flex; gap: 16px; margin: 12px 0; }
            .address-box { flex: 1; border: 1px solid #ddd; padding: 10px; border-radius: 4px; }
            .address-box h3 { font-size: 10px; color: #1a237e; text-transform: uppercase; border-bottom: 1px solid #eee; padding-bottom: 4px; margin-bottom: 6px; }
            .address-box p { margin-bottom: 2px; font-size: 10px; }
            .items-table { width: 100%; border-collapse: collapse; margin: 12px 0; }
            .items-table th { background: #1a237e; color: white; padding: 7px 6px; text-align: center; font-size: 10px; }
            .items-table td { padding: 6px; border-bottom: 1px solid #eee; font-size: 10px; }
            .items-table tbody tr:nth-child(even) { background: #f8f9ff; }
            .center { text-align: center; }
            .right { text-align: right; }
            .totals-section { display: flex; justify-content: space-between; margin: 8px 0; }
            .amount-words { flex: 1; background: #f0f4ff; border: 1px solid #c5cae9; padding: 10px; border-radius: 4px; margin-right: 16px; font-size: 10px; }
            .amount-words-bottom { background: #f0f4ff; border: 1px solid #c5cae9; padding: 10px; border-radius: 4px; margin: 8px 0; font-size: 10px; }
            .totals-table { width: 240px; border-collapse: collapse; margin-left: auto; }
            .totals-table td { padding: 5px 8px; border-bottom: 1px solid #eee; font-size: 10px; }
            .totals-table td:last-child { text-align: right; min-width: 100px; }
            .total-row td { background: #1a237e; color: white; font-size: 11px; }
            .notes-section { background: #fffde7; border-left: 3px solid #f57f17; padding: 8px 12px; margin: 10px 0; font-size: 10px; }
            .notes-section h3 { font-size: 10px; color: #e65100; margin-bottom: 4px; }
            .bank-cheque-row { display: flex; gap: 20px; align-items: flex-start; margin: 10px 0; }
            .bank-section { flex: 1; }
            .bank-section h3 { font-size: 11px; color: #1a237e; margin-bottom: 6px; }
            .bank-table { border-collapse: collapse; font-size: 10px; width: 100%; }
            .bank-table td { padding: 4px 10px; border: 1px solid #ddd; }
            .bank-table td:first-child { background: #e8eaf6; font-weight: bold; width: 150px; }
            .cheque-box { flex: 1; text-align: center; }
            .cheque-label { font-size: 10px; color: #1a237e; font-weight: bold; margin-bottom: 4px; }
            .cheque-img { max-width: 100%; height: auto; border: 1px solid #ddd; border-radius: 4px; }
            .signature-section { display: flex; justify-content: space-between; margin-top: 30px; }
            .sig-box { text-align: center; width: 45%; font-size: 10px; }
            .sig-line { border-top: 1px solid #333; margin: 40px 0 5px; }
            @page { margin: 15mm; }
        """;
    }

    // =====================================================================
    //  MARGIN QUOTATION PDF
    // =====================================================================
    public byte[] generateMarginQuotationPdf(MarginQuotation q) {
        try { return renderPdf(buildMarginQuotationHtml(q)); }
        catch (Exception e) { throw new RuntimeException("PDF failed", e); }
    }

    private String buildMarginQuotationHtml(MarginQuotation q) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>")
          .append(getPdfStyles())
          .append(".margin-col { background:#fff9c4; }")
          .append(".final-col  { background:#e8f5e9; font-weight:bold; }")
          .append("</style></head><body>");

        appendDocHeader(sb, "MARGIN QUOTATION", q.getQuotationNumber(),
                q.getQuotationDate().format(DATE_FMT), q.getValidityPeriod(), q.getStatus());
        appendAddressSection(sb,
                q.getBillToCompany(), q.getBillToGstin(), q.getBillToAddress(),
                q.getShipToCompany(), q.getShipToSiteInfo(), q.getShipToAddress(),
                q.getShipToCity(), q.getShipToState(), q.getShipToPincode());

        sb.append("<table class='items-table'><thead><tr>")
          .append("<th>S.No</th><th>Description</th><th>Qty</th><th>Unit</th>")
          .append("<th>Rate (&#8377;)</th><th>Base Amt (&#8377;)</th>")
          .append("<th class='margin-col'>Margin %</th><th class='margin-col'>Margin Amt (&#8377;)</th>")
          .append("<th class='final-col'>Final Amt (&#8377;)</th>")
          .append("</tr></thead><tbody>");
        for (MarginQuotationItem item : q.getItems()) {
            sb.append("<tr>")
              .append("<td class='center'>").append(item.getSNo()).append("</td>")
              .append("<td>").append(esc(item.getDescription())).append("</td>")
              .append("<td class='right'>").append(fmt(item.getQuantity())).append("</td>")
              .append("<td class='center'>").append(esc(item.getUnit())).append("</td>")
              .append("<td class='right'>").append(fmt(item.getRate())).append("</td>")
              .append("<td class='right'>").append(fmt(item.getBaseAmount())).append("</td>")
              .append("<td class='center margin-col'>").append(fmt(item.getMarginPercent())).append("%</td>")
              .append("<td class='right margin-col'>").append(fmt(item.getMarginAmount())).append("</td>")
              .append("<td class='right final-col'>").append(fmt(item.getFinalAmount())).append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody></table>");

        sb.append("<div class='totals-section'>");
        sb.append("<div class='amount-words'><p><strong>Amount in Words:</strong><br/>")
          .append(numberToWordsUtil.convert(q.getGrandTotal())).append("</p></div>");
        sb.append("<table class='totals-table'>");
        sb.append("<tr><td>Sub Total (Base):</td><td>&#8377; ").append(fmt(q.getSubtotal())).append("</td></tr>");
        sb.append("<tr><td>Total Margin:</td><td>&#8377; ").append(fmt(q.getTotalMarginAmount())).append("</td></tr>");
        sb.append("<tr><td>Total After Margin:</td><td>&#8377; ").append(fmt(q.getTotalAfterMargin())).append("</td></tr>");
        appendGstRows(sb, q.getGstType(), q.getCgstPercent(), q.getCgstAmount(),
                q.getSgstPercent(), q.getSgstAmount(), q.getIgstPercent(), q.getIgstAmount());
        sb.append("<tr class='total-row'><td><strong>Grand Total</strong></td><td><strong>&#8377; ")
          .append(fmt(q.getGrandTotal())).append("</strong></td></tr>");
        sb.append("</table></div>");

        appendNotesAndBank(sb, q.getNotes());
        appendSignature(sb);
        sb.append("</body></html>");
        return sb.toString();
    }

    // =====================================================================
    //  YEARLY QUOTATION PDF
    // =====================================================================
    public byte[] generateYearlyQuotationPdf(YearlyQuotation q) {
        try { return renderPdf(buildYearlyQuotationHtml(q)); }
        catch (Exception e) { throw new RuntimeException("PDF failed", e); }
    }

    private String buildYearlyQuotationHtml(YearlyQuotation q) {
        List<String> years = (q.getYearsCovered() != null)
                ? Arrays.stream(q.getYearsCovered().split(",")).map(String::trim)
                        .filter(s -> !s.isBlank()).collect(Collectors.toList())
                : List.of();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>")
          .append(getPdfStyles())
          .append(".yr-col  { background:#e3f2fd; text-align:right; }")
          .append(".avg-col { background:#ede7f6; font-weight:bold; text-align:right; }")
          .append(".mg-col  { background:#fff9c4; }")
          .append(".fin-col { background:#e8f5e9; font-weight:bold; }")
          .append("</style></head><body>");

        appendDocHeader(sb, "YEARLY ANALYSIS QUOTATION", q.getQuotationNumber(),
                q.getQuotationDate().format(DATE_FMT), q.getValidityPeriod(), q.getStatus());
        sb.append("<p style='font-size:10px;color:#555;margin-bottom:8px;'>")
          .append("Years Covered: <strong>").append(esc(q.getYearsCovered()))
          .append("</strong> &#160;|&#160; Number of Years: <strong>")
          .append(q.getNumberOfYears()).append("</strong></p>");
        appendAddressSection(sb,
                q.getBillToCompany(), q.getBillToGstin(), q.getBillToAddress(),
                q.getShipToCompany(), q.getShipToSiteInfo(), q.getShipToAddress(),
                q.getShipToCity(), q.getShipToState(), q.getShipToPincode());

        sb.append("<table class='items-table'><thead><tr>")
          .append("<th>S.No</th><th>Description</th><th>Unit</th>");
        for (String yr : years) sb.append("<th class='yr-col'>").append(esc(yr)).append(" Qty</th>");
        sb.append("<th class='avg-col'>Avg Qty</th><th>Rate (&#8377;)</th>")
          .append("<th class='yr-col'>Base Amt (&#8377;)</th>")
          .append("<th class='mg-col'>Margin %</th><th class='mg-col'>Margin Amt (&#8377;)</th>")
          .append("<th class='fin-col'>Final Amt (&#8377;)</th>")
          .append("</tr></thead><tbody>");

        for (YearlyQuotationItem item : q.getItems()) {
            List<String> qtys = (item.getYearlyQuantities() == null ? java.util.List.of() :
                java.util.Arrays.stream(item.getYearlyQuantities().split(","))
                    .map(String::trim).filter(s -> !s.isBlank())
                    .collect(Collectors.toList()))
                .stream().map(bd -> { try { return fmt(new java.math.BigDecimal(bd.toString())); } catch(Exception e){ return "0.00"; } })
                .collect(Collectors.toList());
            sb.append("<tr>")
              .append("<td class='center'>").append(item.getSNo()).append("</td>")
              .append("<td>").append(esc(item.getDescription())).append("</td>")
              .append("<td class='center'>").append(esc(item.getUnit())).append("</td>");
            for (int i = 0; i < years.size(); i++) {
                String v = i < qtys.size() ? qtys.get(i) : "0.00";
                sb.append("<td class='yr-col'>").append(v).append("</td>");
            }
            sb.append("<td class='avg-col'>").append(fmt(item.getAverageQuantity())).append("</td>")
              .append("<td class='right'>").append(fmt(item.getRate())).append("</td>")
              .append("<td class='right yr-col'>").append(fmt(item.getBaseAmount())).append("</td>")
              .append("<td class='center mg-col'>").append(fmt(item.getMarginPercent())).append("%</td>")
              .append("<td class='right mg-col'>").append(fmt(item.getMarginAmount())).append("</td>")
              .append("<td class='right fin-col'>").append(fmt(item.getFinalAmount())).append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody></table>");

        sb.append("<div class='totals-section'>");
        sb.append("<div class='amount-words'><p><strong>Amount in Words:</strong><br/>")
          .append(numberToWordsUtil.convert(q.getGrandTotal())).append("</p></div>");
        sb.append("<table class='totals-table'>");
        sb.append("<tr><td>Subtotal (Avg x Rate):</td><td>&#8377; ").append(fmt(q.getSubtotalBeforeMargin())).append("</td></tr>");
        sb.append("<tr><td>Total Margin:</td><td>&#8377; ").append(fmt(q.getTotalMarginAmount())).append("</td></tr>");
        sb.append("<tr><td>Subtotal After Margin:</td><td>&#8377; ").append(fmt(q.getSubtotalAfterMargin())).append("</td></tr>");
        appendGstRows(sb, q.getGstType(), q.getCgstPercent(), q.getCgstAmount(),
                q.getSgstPercent(), q.getSgstAmount(), q.getIgstPercent(), q.getIgstAmount());
        sb.append("<tr class='total-row'><td><strong>Grand Total</strong></td><td><strong>&#8377; ")
          .append(fmt(q.getGrandTotal())).append("</strong></td></tr>");
        sb.append("</table></div>");

        appendNotesAndBank(sb, q.getNotes());
        appendSignature(sb);
        sb.append("</body></html>");
        return sb.toString();
    }

    // =====================================================================
    //  SHARED HELPERS
    // =====================================================================
    private void appendDocHeader(StringBuilder sb, String docType, String docNum,
            String date, String validity, String status) {
        sb.append("<div class='header'>")
          .append("<div class='logo-section'>")
          .append(logoHtml())
          .append("<div class='company-details'>")
          .append("<h1>").append(companyName).append("</h1>")
          .append("<p>GSTIN: ").append(companyGstin).append("</p>")
          .append("<p>").append(companyAddress).append("</p>")
          .append("<p>Ph: ").append(companyPhone).append(" | ").append(companyEmail).append("</p>")
          .append("</div></div>")
          .append("<div class='doc-title'><h2>").append(docType).append("</h2>")
          .append("<p>No: <strong>").append(docNum).append("</strong></p>")
          .append("<p>Date: <strong>").append(date).append("</strong></p>");
        if (validity != null && !validity.isBlank())
            sb.append("<p>Valid: <strong>").append(esc(validity)).append("</strong></p>");
        sb.append("<p>Status: <strong>").append(esc(status)).append("</strong></p>")
          .append("</div></div>");
    }

    private void appendAddressSection(StringBuilder sb,
            String billCo, String billGstin, String billAddr,
            String shipCo, String shipSite, String shipAddr,
            String city, String state, String pin) {
        sb.append("<div class='address-section'>");
        sb.append("<div class='address-box'><h3>Bill To</h3>")
          .append("<p><strong>").append(esc(billCo)).append("</strong></p>");
        if (billGstin != null) sb.append("<p>GSTIN: ").append(esc(billGstin)).append("</p>");
        if (billAddr  != null) sb.append("<p>").append(esc(billAddr)).append("</p>");
        sb.append("</div>");
        sb.append("<div class='address-box'><h3>Consignee (Ship To)</h3>");
        if (shipCo   != null) sb.append("<p><strong>").append(esc(shipCo)).append("</strong></p>");
        if (shipSite != null) sb.append("<p>").append(esc(shipSite)).append("</p>");
        if (shipAddr != null) sb.append("<p>").append(esc(shipAddr)).append("</p>");
        if (city     != null) sb.append("<p>").append(esc(city))
                .append(state != null ? ", " + esc(state) : "")
                .append(pin   != null ? " - " + esc(pin)  : "").append("</p>");
        sb.append("</div></div>");
    }

    private void appendGstRows(StringBuilder sb, String gstType,
            BigDecimal cgstPct, BigDecimal cgstAmt,
            BigDecimal sgstPct, BigDecimal sgstAmt,
            BigDecimal igstPct, BigDecimal igstAmt) {
        if ("IGST".equals(gstType)) {
            sb.append("<tr><td>IGST @ ").append(fmt(igstPct)).append("%</td><td>&#8377; ")
              .append(fmt(igstAmt)).append("</td></tr>");
        } else if (!"NONE".equals(gstType)) {
            if (cgstPct != null && cgstPct.compareTo(BigDecimal.ZERO) > 0)
                sb.append("<tr><td>CGST @ ").append(fmt(cgstPct)).append("%</td><td>&#8377; ")
                  .append(fmt(cgstAmt)).append("</td></tr>");
            if (sgstPct != null && sgstPct.compareTo(BigDecimal.ZERO) > 0)
                sb.append("<tr><td>SGST @ ").append(fmt(sgstPct)).append("%</td><td>&#8377; ")
                  .append(fmt(sgstAmt)).append("</td></tr>");
        }
    }

    /**
     * Appends T&C notes (if provided) and the bank details + cancelled cheque side by side.
     */
    private void appendNotesAndBank(StringBuilder sb, String notes) {
        if (notes != null && !notes.isBlank())
            sb.append("<div class='notes-section'><h3>Terms &amp; Conditions</h3><p>")
              .append(esc(notes)).append("</p></div>");

        // Bank + Cheque row
        sb.append("<div class='bank-cheque-row'>")
          .append("<div class='bank-section'><h3>Bank Details for Payment</h3>")
          .append("<table class='bank-table'>")
          .append("<tr><td>Beneficiary Name</td><td>").append(bankBeneficiary).append("</td></tr>")
          .append("<tr><td>Account No.</td><td>").append(bankAccount).append("</td></tr>")
          .append("<tr><td>IFSC Code</td><td>").append(bankIfsc).append("</td></tr>")
          .append("<tr><td>Bank</td><td>").append(bankName).append("</td></tr>")
          .append("</table></div>")
          .append(chequeHtml())
          .append("</div>");
    }

    private void appendSignature(StringBuilder sb) {
        sb.append("<div class='signature-section'>")
          .append("<div class='sig-box'><p>Prepared By</p><div class='sig-line'></div></div>")
          .append("<div class='sig-box'><p>For ").append(companyName)
          .append("</p><div class='sig-line'></div><p>Authorised Signatory</p></div>")
          .append("</div>");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("\n", "<br/>");
    }
}