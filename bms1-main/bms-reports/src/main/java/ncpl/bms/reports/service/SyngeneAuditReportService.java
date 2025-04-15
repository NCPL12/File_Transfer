package ncpl.bms.reports.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.model.dto.AuditLogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SyngeneAuditReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<AuditLogDTO> fetchAuditLogs(String startDate, String endDate) {
        String query = "SELECT [TIMESTAMP], [OPERATION], [TARGET], [SLOTNAME], [OLDVALUE], [VALUE], [USERNAME] " +
                "FROM [JCIHistorianDB].[dbo].[SynGene_AuditHistory] " +
                "WHERE [TIMESTAMP] BETWEEN ? AND ? ORDER BY [TIMESTAMP] ASC";

        return jdbcTemplate.query(query, new Object[]{startDate, endDate}, (ResultSet rs, int rowNum) -> {
            AuditLogDTO dto = new AuditLogDTO();
            dto.setTimestamp(rs.getString("TIMESTAMP"));
            dto.setOperation(rs.getString("OPERATION"));
            dto.setTarget(rs.getString("TARGET"));
            dto.setSlotName(rs.getString("SLOTNAME"));
            dto.setOldValue(rs.getString("OLDVALUE"));
            dto.setValue(rs.getString("VALUE"));
            dto.setUserName(rs.getString("USERNAME"));
            return dto;
        });
    }

    private String formatTimestamp(String rawTimestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = inputFormat.parse(rawTimestamp);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return outputFormat.format(date);
        } catch (Exception e) {
            log.warn("Invalid timestamp format: " + rawTimestamp, e);
            return rawTimestamp;
        }
    }

    private PdfPCell createCenterCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    public byte[] generateAuditReportPdf(String startDate, String endDate) {
        List<AuditLogDTO> logs = fetchAuditLogs(startDate, endDate);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 60, 50);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    try {
                        PdfContentByte cb = writer.getDirectContent();
                        Font footerFont = new Font(Font.HELVETICA, 9);
                        String generatedOn = "Generated on: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                        String pageNumber = "Page " + writer.getPageNumber();

                        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(generatedOn, footerFont), document.leftMargin(), 30, 0);
                        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(pageNumber, footerFont), (document.right() + document.left()) / 2, 30, 0);
                    } catch (Exception e) {
                        log.error("Footer generation error", e);
                    }
                }
            });

            document.open();

            // Logo + Title Header Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 5f});
            headerTable.setSpacingAfter(10f);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            try {
                Image logo = Image.getInstance(new ClassPathResource("static/images/logo.png").getURL());
                logo.scaleToFit(100, 50);
                logo.setAlignment(Image.ALIGN_LEFT);
                logoCell.addElement(logo);
            } catch (IOException e) {
                log.error("Error loading logo image", e);
            }
            headerTable.addCell(logoCell);
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph titlePara = new Paragraph("Audit Trail Report", titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setIndentationLeft(-80f); // Shift slightly left (tweak the value as needed)

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.addElement(titlePara);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(titleCell);

            document.add(headerTable);

            // Date Formatting
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

            String formattedStartDate = startDate;
            String formattedEndDate = endDate;

            try {
                Date start = inputFormat.parse(startDate);
                Date end = inputFormat.parse(endDate);
                formattedStartDate = outputFormat.format(start);
                formattedEndDate = outputFormat.format(end);
            } catch (Exception e) {
                log.warn("Unable to format start/end dates", e);
            }

            Font dateFont = new Font(Font.HELVETICA, 11);
            PdfPTable dateTable = new PdfPTable(2);
            dateTable.setWidthPercentage(100);
            dateTable.setWidths(new float[]{1f, 1f});
            dateTable.setSpacingBefore(4f);
            dateTable.setSpacingAfter(4f);

            PdfPCell leftCell = new PdfPCell(new Phrase("Start Date: " + formattedStartDate, dateFont));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell rightCell = new PdfPCell(new Phrase("End Date: " + formattedEndDate, dateFont));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            dateTable.addCell(leftCell);
            dateTable.addCell(rightCell);

            document.add(dateTable);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 4f, 2f, 2f, 2f, 2f});
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 9);

            String[] headers = {"Timestamp", "Operation", "Target", "Slot Name", "Old Value", "Value", "User Name"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }
            table.setHeaderRows(1);

            for (AuditLogDTO log : logs) {
                table.addCell(createCenterCell(formatTimestamp(log.getTimestamp()), cellFont));
                table.addCell(createCenterCell(log.getOperation(), cellFont));
                table.addCell(createCenterCell(log.getTarget(), cellFont));
                table.addCell(createCenterCell(log.getSlotName(), cellFont));
                table.addCell(createCenterCell(log.getOldValue(), cellFont));
                table.addCell(createCenterCell(log.getValue(), cellFont));
                table.addCell(createCenterCell(log.getUserName(), cellFont));
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating audit report PDF", e);
            return null;
        }
    }
}
