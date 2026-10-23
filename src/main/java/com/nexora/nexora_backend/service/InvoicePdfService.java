package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.entity.Invoice;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.InvoiceRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import lombok.RequiredArgsConstructor;
import java.awt.Color;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final OrganizationContext organizationContext;
    private final OrganizationBrandingService brandingService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    Locale.ENGLISH
            );

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-IN")
            );

    static {
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
    }

    /**
     * Generate invoice PDF for the current organization.
     */
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long invoiceId) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Invoice invoice =
                invoiceRepository
                        .findByIdAndOrganizationId(
                                invoiceId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invoice not found"
                                )
                        );

        return buildPdf(invoice);
    }

    private byte[] buildPdf(Invoice invoice) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document(
                    PageSize.A4,
                    42,
                    42,
                    42,
                    42
            );

            PdfWriter.getInstance(document, outputStream);

            document.open();

            addHeader(document, invoice);
            addInvoiceInformation(document, invoice);
            addCustomerInformation(document, invoice);
            addChargesTable(document, invoice);
            addTotals(document, invoice);
            addFooter(document);

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException | IOException exception) {
            throw new IllegalStateException(
                    "Unable to generate invoice PDF",
                    exception
            );
        }
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void addHeader(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        PdfPTable header =
                new PdfPTable(
                        new float[]{65, 35}
                );

        header.setWidthPercentage(100);

        header.setSpacingAfter(22);


        // Company
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(PdfPCell.NO_BORDER);

        var organization = invoice.getOrganization();

        byte[] logoBytes = organization == null ? null
                : brandingService.getLogoBytesForOrganization(organization.getId());
        if (logoBytes != null) {
            try {
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(110, 48);
                logo.setSpacingAfter(6);
                companyCell.addElement(logo);
            } catch (Exception ignored) {
                // A missing/invalid optional logo must not prevent invoice generation.
            }
        }

        String companyName = organization != null
                && organization.getName() != null
                && !organization.getName().isBlank()
                ? organization.getName().trim()
                : "Company";

        String companyEmail = organization != null
                ? safe(organization.getEmail())
                : "-";

        String companyPhone = organization != null
                ? safe(organization.getPhone())
                : "-";

        String companyAddress = organization != null
                ? safe(organization.getAddress())
                : "-";

        Font companyFont = new Font(
                Font.HELVETICA,
                20,
                Font.BOLD,
                new Color(37, 99, 235)
        );

        Font companyDetailsFont = new Font(
                Font.HELVETICA,
                9,
                Font.NORMAL,
                new Color(100, 116, 139)
        );

        Paragraph companyNameParagraph =
                new Paragraph(companyName, companyFont);

        companyNameParagraph.setSpacingAfter(4);
        companyCell.addElement(companyNameParagraph);

        companyCell.addElement(
                new Paragraph(companyAddress, companyDetailsFont)
        );

        companyCell.addElement(
                new Paragraph(
                        "Email: " + companyEmail
                                + " | Phone: " + companyPhone,
                        companyDetailsFont
                )
        );

        header.addCell(companyCell);


        // Invoice title
        PdfPCell titleCell =
                new PdfPCell();

        titleCell.setBorder(
                PdfPCell.NO_BORDER
        );

        titleCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        Paragraph invoiceTitle =
                new Paragraph(
                        "INVOICE",
                        new Font(
                                Font.HELVETICA,
                                22,
                                Font.BOLD,
                                new Color(
                                        15,
                                        23,
                                        42
                                )
                        )
                );

        invoiceTitle.setAlignment(
                Element.ALIGN_RIGHT
        );

        titleCell.addElement(
                invoiceTitle
        );

        Paragraph invoiceNumber =
                new Paragraph(
                        invoice.getInvoiceNumber(),
                        new Font(
                                Font.HELVETICA,
                                10,
                                Font.NORMAL,
                                new Color(
                                        100,
                                        116,
                                        139
                                )
                        )
                );

        invoiceNumber.setAlignment(
                Element.ALIGN_RIGHT
        );

        titleCell.addElement(
                invoiceNumber
        );

        header.addCell(
                titleCell
        );

        document.add(
                header
        );
    }

    // =========================================================
    // INVOICE INFORMATION
    // =========================================================

    private void addInvoiceInformation(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        PdfPTable table =
                new PdfPTable(3);

        table.setWidthPercentage(100);

        table.setSpacingAfter(18);

        addInfoCell(
                table,
                "Invoice Number",
                invoice.getInvoiceNumber()
        );

        addInfoCell(
                table,
                "Status",
                safe(invoice.getStatus())
        );

        addInfoCell(
                table,
                "Job Number",
                invoice.getJob() != null
                        ? safe(invoice.getJob().getJobNumber())
                        : "-"
        );

        addInfoCell(
                table,
                "Issued Date",
                formatDate(invoice.getIssuedAt())
        );

        addInfoCell(
                table,
                "Due Date",
                formatDate(invoice.getDueDate())
        );

        addInfoCell(
                table,
                "Payment Status",
                safe(invoice.getPaymentStatus())
        );

        document.add(
                table
        );
    }

    // =========================================================
    // CUSTOMER
    // =========================================================

    private void addCustomerInformation(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        Paragraph heading =
                new Paragraph(
                        "BILL TO",
                        new Font(
                                Font.HELVETICA,
                                10,
                                Font.BOLD,
                                new Color(
                                        37,
                                        99,
                                        235
                                )
                        )
                );

        heading.setSpacingAfter(6);

        document.add(
                heading
        );

        String customerName = "-";

        if (
                invoice.getJob() != null
                        && invoice.getJob().getCustomer() != null
        ) {
            customerName =
                    safe(
                            invoice
                                    .getJob()
                                    .getCustomer()
                                    .getName()
                    );
        }

        Paragraph customer =
                new Paragraph(
                        customerName,
                        new Font(
                                Font.HELVETICA,
                                13,
                                Font.BOLD,
                                new Color(
                                        15,
                                        23,
                                        42
                                )
                        )
                );

        customer.setSpacingAfter(18);

        document.add(
                customer
        );
    }

    // =========================================================
    // CHARGES
    // =========================================================

    private void addChargesTable(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        PdfPTable table =
                new PdfPTable(
                        new float[]{70, 30}
                );

        table.setWidthPercentage(100);

        table.setSpacingBefore(8);

        table.setSpacingAfter(18);

        addTableHeader(
                table,
                "Description",
                "Amount"
        );

        addAmountRow(
                table,
                "Labour Charges",
                invoice.getLabourAmount()
        );

        addAmountRow(
                table,
                "Parts",
                invoice.getPartsAmount()
        );

        addAmountRow(
                table,
                "Other Charges",
                invoice.getOtherCharges()
        );

        addAmountRow(
                table,
                "Discount",
                safeAmount(invoice.getDiscount()).negate()
        );

        document.add(
                table
        );
    }

    // =========================================================
    // TOTALS
    // =========================================================

    private void addTotals(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        PdfPTable totals =
                new PdfPTable(
                        new float[]{70, 30}
                );

        totals.setWidthPercentage(100);

        addTotalRow(
                totals,
                "Subtotal",
                invoice.getSubtotal(),
                false
        );

        addTotalRow(
                totals,
                "Tax",
                invoice.getTaxAmount(),
                false
        );

        addTotalRow(
                totals,
                "Total Amount",
                invoice.getTotalAmount(),
                true
        );

        document.add(
                totals
        );
    }

    // =========================================================
    // FOOTER
    // =========================================================

    private void addFooter(
            Document document
    ) throws DocumentException {

        Paragraph spacing =
                new Paragraph(
                        " "
                );

        spacing.setSpacingBefore(28);

        document.add(
                spacing
        );

        Paragraph footer =
                new Paragraph(
                        "Thank you for your business.",
                        new Font(
                                Font.HELVETICA,
                                9,
                                Font.NORMAL,
                                new Color(
                                        100,
                                        116,
                                        139
                                )
                        )
                );

        footer.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(
                footer
        );

        Paragraph generated =
                new Paragraph(
                        "This is a computer-generated invoice.",
                        new Font(
                                Font.HELVETICA,
                                8,
                                Font.NORMAL,
                                new Color(
                                        148,
                                        163,
                                        184
                                )
                        )
                );

        generated.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(
                generated
        );

        var branding = brandingService.getSettings();
        if (branding.invoiceFooterText() != null && !branding.invoiceFooterText().isBlank()) {
            Paragraph customFooter = new Paragraph(branding.invoiceFooterText(),
                    new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 116, 139)));
            customFooter.setAlignment(Element.ALIGN_CENTER);
            document.add(customFooter);
        }
        if (branding.showNexoraAttribution()) {
            Paragraph attribution = new Paragraph("Powered by NEXORA",
                    new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(148, 163, 184)));
            attribution.setAlignment(Element.ALIGN_CENTER);
            document.add(attribution);
        }
    }

    // =========================================================
    // TABLE HELPERS
    // =========================================================

    private void addTableHeader(
            PdfPTable table,
            String left,
            String right
    ) {

        PdfPCell leftCell =
                createHeaderCell(left);

        PdfPCell rightCell =
                createHeaderCell(right);

        rightCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        table.addCell(leftCell);
        table.addCell(rightCell);
    }

    private PdfPCell createHeaderCell(
            String text
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                new Font(
                                        Font.HELVETICA,
                                        9,
                                        Font.BOLD,
                                        Color.WHITE
                                )
                        )
                );

        cell.setBackgroundColor(
                new Color(
                        37,
                        99,
                        235
                )
        );

        cell.setPadding(8);

        cell.setBorderColor(
                new Color(
                        37,
                        99,
                        235
                )
        );

        return cell;
    }

    private void addAmountRow(
            PdfPTable table,
            String description,
            BigDecimal amount
    ) {

        PdfPCell descriptionCell =
                new PdfPCell(
                        new Phrase(
                                description,
                                normalFont()
                        )
                );

        PdfPCell amountCell =
                new PdfPCell(
                        new Phrase(
                                formatCurrency(
                                        amount
                                ),
                                normalFont()
                        )
                );

        descriptionCell.setPadding(8);
        amountCell.setPadding(8);

        amountCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        descriptionCell.setBorderColor(
                new Color(
                        226,
                        232,
                        240
                )
        );

        amountCell.setBorderColor(
                new Color(
                        226,
                        232,
                        240
                )
        );

        table.addCell(
                descriptionCell
        );

        table.addCell(
                amountCell
        );
    }

    private void addTotalRow(
            PdfPTable table,
            String label,
            BigDecimal amount,
            boolean grandTotal
    ) {

        Font font =
                new Font(
                        Font.HELVETICA,
                        grandTotal ? 13 : 10,
                        grandTotal
                                ? Font.BOLD
                                : Font.NORMAL,
                        grandTotal
                                ? new Color(
                                15,
                                23,
                                42
                        )
                                : new Color(
                                51,
                                65,
                                85
                        )
                );

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                font
                        )
                );

        PdfPCell amountCell =
                new PdfPCell(
                        new Phrase(
                                formatCurrency(amount),
                                font
                        )
                );

        labelCell.setPadding(
                grandTotal ? 10 : 6
        );

        amountCell.setPadding(
                grandTotal ? 10 : 6
        );

        amountCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        if (grandTotal) {

            Color background =
                    new Color(
                            239,
                            246,
                            255
                    );

            labelCell.setBackgroundColor(
                    background
            );

            amountCell.setBackgroundColor(
                    background
            );
        }

        table.addCell(
                labelCell
        );

        table.addCell(
                amountCell
        );
    }

    private void addInfoCell(
            PdfPTable table,
            String label,
            String value
    ) {

        PdfPCell cell =
                new PdfPCell();

        cell.setPadding(9);

        cell.setBorderColor(
                new Color(
                        226,
                        232,
                        240
                )
        );

        Paragraph labelParagraph =
                new Paragraph(
                        label,
                        new Font(
                                Font.HELVETICA,
                                8,
                                Font.BOLD,
                                new Color(
                                        100,
                                        116,
                                        139
                                )
                        )
                );

        Paragraph valueParagraph =
                new Paragraph(
                        value,
                        new Font(
                                Font.HELVETICA,
                                10,
                                Font.NORMAL,
                                new Color(
                                        15,
                                        23,
                                        42
                                )
                        )
                );

        cell.addElement(
                labelParagraph
        );

        cell.addElement(
                valueParagraph
        );

        table.addCell(
                cell
        );
    }

    private Font normalFont() {

        return new Font(
                Font.HELVETICA,
                9,
                Font.NORMAL,
                new Color(
                        51,
                        65,
                        85
                )
        );
    }

    // =========================================================
    // FORMATTERS
    // =========================================================

    private String formatCurrency(
            BigDecimal amount
    ) {

        return CURRENCY_FORMAT.format(
                safeAmount(amount)
        );
    }

    private String formatDate(
            LocalDate date
    ) {

        if (date == null) {
            return "-";
        }

        return DATE_FORMATTER.format(
                date
        );
    }

    private BigDecimal safeAmount(
            BigDecimal amount
    ) {

        return amount != null
                ? amount
                : BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String safe(
            String value
    ) {

        return value != null
                && !value.isBlank()
                ? value
                : "-";
    }
}