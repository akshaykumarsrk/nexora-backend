package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.entity.Invoice;
import com.nexora.nexora_backend.entity.Payment;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.PaymentRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import lombok.RequiredArgsConstructor;
import java.awt.Color;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentReceiptService {
    private final PaymentRepository paymentRepository;

    private void addSummaryRow(
            PdfPTable table,
            String label,
            BigDecimal amount,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        labelCell.setBorder(
                PdfPCell.BOTTOM
        );

        labelCell.setPadding(8);

        table.addCell(labelCell);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                formatCurrency(amount),
                                valueFont
                        )
                );

        valueCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        valueCell.setBorder(
                PdfPCell.BOTTOM
        );

        valueCell.setPadding(8);

        table.addCell(valueCell);
    }

    private final OrganizationContext organizationContext;
    private final OrganizationBrandingService brandingService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Transactional(readOnly = true)
    public byte[] generateReceipt(Long paymentId) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Payment payment =
                paymentRepository
                        .findByIdAndOrganizationId(
                                paymentId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found"
                                )
                        );

        Invoice invoice =
                payment.getInvoice();

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "Payment is not linked to an invoice"
            );
        }

        BigDecimal invoiceTotal = moneyOrZero(invoice.getTotalAmount());

        BigDecimal totalPaid = calculateTotalPaid(invoice, organizationId);

        BigDecimal remaining = invoiceTotal
                .subtract(totalPaid)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        boolean fullyPaid = remaining.signum() == 0;

        String receiptNumber =
                "RCT-" + payment.getId();

        return buildPdf(
                payment,
                invoice,
                invoiceTotal,
                totalPaid,
                remaining,
                fullyPaid,
                receiptNumber
        );
    }

    private byte[] buildPdf(
            Payment payment,
            Invoice invoice,
            BigDecimal invoiceTotal,
            BigDecimal totalPaid,
            BigDecimal remaining,
            boolean fullyPaid,
            String receiptNumber
    ) {

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(
                            PageSize.A4,
                            42,
                            42,
                            42,
                            42
                    );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            // =================================================
            // FONTS
            // =================================================

            Font brandFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            22,
                            Font.BOLD
                    );

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Font.BOLD
                    );

            Font subtitleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            9,
                            Font.NORMAL
                    );

            Font sectionFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            11,
                            Font.BOLD
                    );

            Font labelFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            9,
                            Font.NORMAL
                    );

            Font valueFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            9,
                            Font.BOLD
                    );

            Font amountFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            20,
                            Font.BOLD
                    );

            Font footerFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            8,
                            Font.NORMAL
                    );

            // =================================================
            // HEADER
            // =================================================

            PdfPTable header =
                    new PdfPTable(2);

            header.setWidthPercentage(100);
            header.setWidths(new float[]{60, 40});

            PdfPCell brandCell =
                    new PdfPCell();

            brandCell.setBorder(
                    PdfPCell.NO_BORDER
            );


            var organization = invoice.getOrganization();

            byte[] logoBytes = brandingService.getCurrentLogoBytes();
            if (logoBytes != null) {
                try {
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(110, 48);
                    logo.setSpacingAfter(6);
                    brandCell.addElement(logo);
                } catch (Exception ignored) {
                    // A missing/invalid optional logo must not prevent receipt generation.
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

            Paragraph brand = new Paragraph(
                    companyName,
                    brandFont
            );

            brand.setSpacingAfter(3);
            brandCell.addElement(brand);

            brandCell.addElement(
                    new Paragraph(companyAddress, subtitleFont)
            );

            brandCell.addElement(
                    new Paragraph(
                            "Email: " + companyEmail
                                    + " | Phone: " + companyPhone,
                            subtitleFont
                    )
            );

            header.addCell(brandCell);

            PdfPCell receiptCell =
                    new PdfPCell();

            receiptCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            receiptCell.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            Paragraph receiptTitle =
                    new Paragraph(
                            "PAYMENT RECEIPT",
                            titleFont
                    );

            receiptTitle.setAlignment(
                    Element.ALIGN_RIGHT
            );

            receiptCell.addElement(receiptTitle);

            Paragraph receiptNumberParagraph =
                    new Paragraph(
                            receiptNumber,
                            subtitleFont
                    );

            receiptNumberParagraph.setAlignment(
                    Element.ALIGN_RIGHT
            );

            receiptCell.addElement(
                    receiptNumberParagraph
            );

            header.addCell(receiptCell);

            document.add(header);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // RECEIPT STATUS
            // =================================================

            PdfPTable statusTable =
                    new PdfPTable(1);

            statusTable.setWidthPercentage(100);

            PdfPCell statusCell =
                    new PdfPCell();

            statusCell.setBackgroundColor(
                    fullyPaid
                            ? new Color(232, 245, 233)
                            : new Color(255, 243, 224)
            );

            statusCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            statusCell.setPadding(12);

            String statusText =
                    fullyPaid
                            ? "PAYMENT RECEIVED — FULLY PAID"
                            : "PAYMENT RECEIVED — PARTIAL PAYMENT";

            Font statusFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10,
                            Font.BOLD
                    );

            Paragraph status =
                    new Paragraph(
                            statusText,
                            statusFont
                    );

            status.setAlignment(
                    Element.ALIGN_CENTER
            );

            statusCell.addElement(status);

            statusTable.addCell(statusCell);

            document.add(statusTable);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // PAYMENT INFORMATION
            // =================================================

            document.add(
                    new Paragraph(
                            "Payment Information",
                            sectionFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            PdfPTable paymentInfo =
                    new PdfPTable(2);

            paymentInfo.setWidthPercentage(100);
            paymentInfo.setWidths(
                    new float[]{30, 70}
            );

            addInfoRow(
                    paymentInfo,
                    "Receipt Number",
                    receiptNumber,
                    labelFont,
                    valueFont
            );

            addInfoRow(
                    paymentInfo,
                    "Payment Date",
                    formatDate(
                            payment.getPaymentDate()
                    ),
                    labelFont,
                    valueFont
            );

            addInfoRow(
                    paymentInfo,
                    "Payment Method",
                    formatPaymentMethod(
                            payment.getPaymentMethod()
                    ),
                    labelFont,
                    valueFont
            );

            addInfoRow(
                    paymentInfo,
                    "Transaction Reference",
                    safe(
                            payment.getTransactionReference()
                    ),
                    labelFont,
                    valueFont
            );

            document.add(paymentInfo);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // CUSTOMER / INVOICE INFORMATION
            // =================================================

            document.add(
                    new Paragraph(
                            "Invoice Information",
                            sectionFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            PdfPTable invoiceInfo =
                    new PdfPTable(2);

            invoiceInfo.setWidthPercentage(100);
            invoiceInfo.setWidths(
                    new float[]{30, 70}
            );

            addInfoRow(
                    invoiceInfo,
                    "Invoice Number",
                    safe(invoice.getInvoiceNumber()),
                    labelFont,
                    valueFont
            );

            if (invoice.getJob() != null) {

                addInfoRow(
                        invoiceInfo,
                        "Job Number",
                        safe(
                                invoice.getJob()
                                        .getJobNumber()
                        ),
                        labelFont,
                        valueFont
                );

                if (
                        invoice.getJob().getCustomer()
                                != null
                ) {

                    addInfoRow(
                            invoiceInfo,
                            "Customer",
                            safe(
                                    invoice.getJob()
                                            .getCustomer()
                                            .getName()
                            ),
                            labelFont,
                            valueFont
                    );
                }
            }

            document.add(invoiceInfo);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // PAYMENT AMOUNT
            // =================================================

            PdfPTable amountTable =
                    new PdfPTable(1);

            amountTable.setWidthPercentage(100);

            PdfPCell amountCell =
                    new PdfPCell();

            amountCell.setPadding(18);
            amountCell.setBackgroundColor(
                    new Color(245, 247, 250)
            );

            amountCell.setBorderColor(
                    new Color(220, 224, 230)
            );

            Paragraph amountLabel =
                    new Paragraph(
                            "AMOUNT RECEIVED",
                            sectionFont
                    );

            amountLabel.setAlignment(
                    Element.ALIGN_CENTER
            );

            amountCell.addElement(
                    amountLabel
            );

            Paragraph amount =
                    new Paragraph(
                            formatCurrency(
                                    payment.getAmount()
                            ),
                            amountFont
                    );

            amount.setAlignment(
                    Element.ALIGN_CENTER
            );

            amountCell.addElement(amount);

            amountTable.addCell(amountCell);

            document.add(amountTable);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // PAYMENT SUMMARY
            // =================================================

            PdfPTable summary =
                    new PdfPTable(2);

            summary.setWidthPercentage(100);
            summary.setWidths(
                    new float[]{65, 35}
            );

            addSummaryRow(
                    summary,
                    "Invoice Total",
                    invoiceTotal,
                    labelFont,
                    valueFont
            );

            addSummaryRow(
                    summary,
                    "Total Paid",
                    totalPaid,
                    labelFont,
                    valueFont
            );

            addSummaryRow(
                    summary,
                    "Remaining Balance",
                    remaining,
                    labelFont,
                    valueFont
            );

            document.add(summary);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // NOTES
            // =================================================

            if (
                    payment.getNotes() != null
                            && !payment.getNotes()
                            .trim()
                            .isEmpty()
            ) {

                document.add(
                        new Paragraph(
                                "Notes",
                                sectionFont
                        )
                );

                Paragraph notes =
                        new Paragraph(
                                payment.getNotes(),
                                labelFont
                        );

                notes.setSpacingBefore(6);
                notes.setSpacingAfter(10);

                document.add(notes);
            }

            // =================================================
            // FOOTER
            // =================================================

            document.add(
                    new Paragraph(" ")
            );

            PdfPTable footer =
                    new PdfPTable(1);

            footer.setWidthPercentage(100);

            PdfPCell footerCell =
                    new PdfPCell();

            footerCell.setBorder(
                    PdfPCell.TOP
            );

            footerCell.setBorderColor(
                    new Color(220, 224, 230)
            );

            footerCell.setPaddingTop(12);

            Paragraph thankYou =
                    new Paragraph(
                            "Thank you for your payment.",
                            sectionFont
                    );

            thankYou.setAlignment(
                    Element.ALIGN_CENTER
            );

            footerCell.addElement(
                    thankYou
            );

            Paragraph generated =
                    new Paragraph(
                            "This is a computer-generated payment receipt.",
                            footerFont
                    );

            generated.setAlignment(
                    Element.ALIGN_CENTER
            );

            footerCell.addElement(
                    generated
            );

            var branding = brandingService.getSettings();
            if (branding.receiptFooterText() != null && !branding.receiptFooterText().isBlank()) {
                Paragraph customFooter = new Paragraph(branding.receiptFooterText(),
                        new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 116, 139)));
                customFooter.setAlignment(Element.ALIGN_CENTER);
                footerCell.addElement(customFooter);
            }
            if (branding.showNexoraAttribution()) {
                Paragraph attribution = new Paragraph("Powered by NEXORA", footerFont);
                attribution.setAlignment(Element.ALIGN_CENTER);
                footerCell.addElement(attribution);
            }

            footer.addCell(
                    footerCell
            );

            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {

            throw new IllegalStateException(
                    "Unable to generate payment receipt",
                    e
            );
        }
    }

    private void addInfoRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        labelCell.setBorder(
                PdfPCell.NO_BORDER
        );

        labelCell.setPadding(6);

        table.addCell(labelCell);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                value,
                                valueFont
                        )
                );

        valueCell.setBorder(
                PdfPCell.NO_BORDER
        );

        valueCell.setPadding(6);

        table.addCell(valueCell);
    }


    private BigDecimal calculateTotalPaid(
            Invoice invoice,
            Long organizationId
    ) {
        return moneyOrZero(
                paymentRepository.getTotalPaidForInvoice(
                        invoice.getId(),
                        organizationId
                )
        );
    }

    private BigDecimal moneyOrZero(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("en-IN")
                );

        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);

        return currencyFormat.format(
                amount != null
                        ? amount
                        : BigDecimal.ZERO
        );
    }


    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return "-";
        }

        return date.format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
        );
    }

    private String formatPaymentMethod(
            String method
    ) {

        if (method == null
                || method.isBlank()) {

            return "-";
        }

        String formatted =
                method
                        .replace("_", " ")
                        .toLowerCase();

        return Character.toUpperCase(
                formatted.charAt(0)
        ) + formatted.substring(1);
    }

    private String safe(
            String value
    ) {

        return value == null
                || value.isBlank()
                ? "-"
                : value;
    }
}