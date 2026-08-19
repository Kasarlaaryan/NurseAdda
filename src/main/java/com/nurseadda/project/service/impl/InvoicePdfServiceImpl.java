package com.nurseadda.project.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.nurseadda.project.entity.Invoice;
import com.nurseadda.project.service.InvoicePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final BaseColor BRAND_BLUE = new BaseColor(41, 128, 185);
    private static final BaseColor TABLE_HEADER_BG = new BaseColor(52, 73, 94);
    private static final BaseColor TABLE_ALT_ROW = new BaseColor(245, 245, 245);
    private static final BaseColor GRAY = BaseColor.GRAY;
    private static final BaseColor WHITE = BaseColor.WHITE;
    private static final BaseColor GREEN = new BaseColor(39, 174, 96);
    private static final BaseColor AMBER = new BaseColor(243, 156, 18);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Override
    public ByteArrayOutputStream generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, invoice);
            addInvoiceDetails(document, invoice);
            addLineItemsTable(document, invoice);
            addTotals(document, invoice);
            addFooter(document, invoice);

            document.close();
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice {}: {}", invoice.getId(), e.getMessage());
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }

        return out;
    }

    private void addHeader(Document document, Invoice invoice) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{70, 30});

        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND_BLUE);
        Paragraph companyName = new Paragraph("NurseAdda", companyFont);
        companyName.setSpacingAfter(4);

        Font taglineFont = FontFactory.getFont(FontFactory.HELVETICA, 9, GRAY);
        Paragraph tagline = new Paragraph("Healthcare Workforce Management", taglineFont);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(companyName);
        leftCell.addElement(tagline);
        headerTable.addCell(leftCell);

        Font invoiceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, BRAND_BLUE);
        Paragraph invoiceLabel = new Paragraph("INVOICE", invoiceFont);
        invoiceLabel.setAlignment(Element.ALIGN_RIGHT);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.addElement(invoiceLabel);
        rightCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);
    }

    private void addInvoiceDetails(Document document, Invoice invoice) throws DocumentException {
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{50, 50});
        detailsTable.setSpacingAfter(12);

        String clientName = invoice.getClient().getUser().getFirstName() + " " + invoice.getClient().getUser().getLastName();
        String clientEmail = invoice.getClient().getUser().getEmail();

        PdfPTable leftTable = new PdfPTable(2);
        leftTable.setWidths(new float[]{35, 65});
        leftTable.addCell(createLabelCell("BILL TO:"));
        leftTable.addCell(createValueCell(clientName + "\n" + clientEmail));
        leftTable.addCell(createLabelCell("INVOICE NO:"));
        leftTable.addCell(createValueCell("INV-" + String.format("%06d", invoice.getId())));

        PdfPTable rightTable = new PdfPTable(2);
        rightTable.setWidths(new float[]{35, 65});
        rightTable.addCell(createLabelCell("DATE:"));
        rightTable.addCell(createValueCell(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(DATE_FORMAT) : "N/A"));
        rightTable.addCell(createLabelCell("STATUS:"));
        rightTable.addCell(createStatusCell(invoice.getStatus()));

        PdfPCell left = new PdfPCell(leftTable);
        left.setBorder(Rectangle.NO_BORDER);
        PdfPCell right = new PdfPCell(rightTable);
        right.setBorder(Rectangle.NO_BORDER);

        detailsTable.addCell(left);
        detailsTable.addCell(right);

        document.add(detailsTable);
    }

    private void addLineItemsTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 20, 20, 20});
        table.setSpacingBefore(6);
        table.setSpacingAfter(6);

        String[] headers = {"Description", "Hours", "Rate (\u20B9)", "Amount (\u20B9)"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE)));
            cell.setBackgroundColor(TABLE_HEADER_BG);
            cell.setPadding(8);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(cell);
        }

        addTableRow(table, "Regular Hours",
                invoice.getBaseHours().toString(),
                formatCurrency(invoice.getClientHourlyRate()),
                formatCurrency(invoice.getBaseAmount()), false);

        if (invoice.getOvertimeHours().doubleValue() > 0) {
            addTableRow(table, "Overtime Hours (1.5x)",
                    invoice.getOvertimeHours().toString(),
                    formatCurrency(invoice.getClientHourlyRate().multiply(new BigDecimal("1.5"))),
                    formatCurrency(invoice.getOvertimeAmount()), true);
        }

        if (invoice.getAssignment() != null && invoice.getAssignment().getStaffingRequest() != null) {
            String designation = invoice.getAssignment().getStaffingRequest().getDesignation();
            String location = invoice.getAssignment().getStaffingRequest().getLocation();
            PdfPCell descCell = new PdfPCell(new Phrase(
                    "Assignment: " + (designation != null ? designation : "N/A") + " - " + (location != null ? location : "N/A"),
                    FontFactory.getFont(FontFactory.HELVETICA, 9, GRAY)));
            descCell.setPadding(8);
            descCell.setColspan(4);
            descCell.setBackgroundColor(TABLE_ALT_ROW);
            table.addCell(descCell);
        }

        document.add(table);
    }

    private void addTableRow(PdfPTable table, String description, String hours, String rate, String amount, boolean altRow) {
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        PdfPCell descCell = new PdfPCell(new Phrase(description, dataFont));
        PdfPCell hoursCell = new PdfPCell(new Phrase(hours, dataFont));
        PdfPCell rateCell = new PdfPCell(new Phrase(rate, dataFont));
        PdfPCell amountCell = new PdfPCell(new Phrase(amount, boldFont));

        PdfPCell[] cells = {descCell, hoursCell, rateCell, amountCell};
        for (PdfPCell cell : cells) {
            cell.setPadding(8);
            if (altRow) cell.setBackgroundColor(TABLE_ALT_ROW);
        }
        hoursCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        rateCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        amountCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);

        for (PdfPCell cell : cells) table.addCell(cell);
    }

    private void addTotals(Document document, Invoice invoice) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setWidths(new float[]{60, 40});
        totalsTable.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(8);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_BLUE);

        addTotalRow(totalsTable, "Subtotal:", formatCurrency(invoice.getBaseAmount()), labelFont);
        if (invoice.getOvertimeAmount().doubleValue() > 0) {
            addTotalRow(totalsTable, "Overtime:", formatCurrency(invoice.getOvertimeAmount()), labelFont);
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL:", totalFont));
        totalLabel.setBorder(PdfPCell.TOP);
        totalLabel.setPadding(8);
        PdfPCell totalValue = new PdfPCell(new Phrase(formatCurrency(invoice.getTotalAmount()), totalFont));
        totalValue.setBorder(PdfPCell.TOP);
        totalValue.setPadding(8);
        totalValue.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);

        totalsTable.addCell(totalLabel);
        totalsTable.addCell(totalValue);

        document.add(totalsTable);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document, Invoice invoice) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, GRAY);
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.add(new Chunk("Thank you for your business!", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLUE)));
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("NurseAdda - Healthcare Workforce Management | This is a system-generated invoice.", footerFont));

        document.add(footer);
    }

    private PdfPCell createLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, GRAY)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        return cell;
    }

    private PdfPCell createValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        return cell;
    }

    private PdfPCell createStatusCell(String status) {
        BaseColor color = "PAID".equals(status) ? GREEN : "PENDING".equals(status) ? AMBER : GRAY;
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color);
        PdfPCell cell = new PdfPCell(new Phrase(status, statusFont));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        return cell;
    }

    private String formatCurrency(BigDecimal amount) {
        return "\u20B9" + String.format("%,.2f", amount);
    }
}
