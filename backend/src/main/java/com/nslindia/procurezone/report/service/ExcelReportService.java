package com.nslindia.procurezone.report.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generic Excel Report Generation Service using Apache POI
 * Provides methods to create formatted Excel files with headers, data, and
 * styling
 */
@Service
@Slf4j
public class ExcelReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Generate Excel file from data with headers
     * 
     * @param headers   List of column headers
     * @param data      List of data rows (each row is a map of column->value)
     * @param sheetName Name of the Excel sheet
     * @param title     Optional title for the report
     * @return Excel file as byte array
     */
    public byte[] generateExcelReport(List<String> headers,
            List<Map<String, Object>> data,
            String sheetName,
            String title) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Create cell styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int rowNum = 0;

            // Add title if provided
            if (title != null && !title.isEmpty()) {
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(title);
                titleCell.setCellStyle(titleStyle);

                // Merge cells for title
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));
                rowNum++; // Empty row after title
            }

            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (Map<String, Object> rowData : data) {
                Row dataRow = sheet.createRow(rowNum++);

                int colNum = 0;
                for (String header : headers) {
                    Cell cell = dataRow.createCell(colNum++);
                    Object value = rowData.get(header);

                    setCellValue(cell, value, dataStyle, numberStyle, dateStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate Excel report with multiple sheets
     */
    public byte[] generateMultiSheetExcelReport(
            Map<String, SheetData> sheets) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Create cell styles once for all sheets
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create each sheet
            for (Map.Entry<String, SheetData> entry : sheets.entrySet()) {
                String sheetName = entry.getKey();
                SheetData sheetData = entry.getValue();

                createSheet(workbook, sheetName, sheetData,
                        titleStyle, headerStyle, dataStyle, numberStyle, dateStyle);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create a single sheet in the workbook
     */
    private void createSheet(Workbook workbook, String sheetName, SheetData sheetData,
            CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle,
            CellStyle numberStyle, CellStyle dateStyle) {

        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;

        // Add title if provided
        if (sheetData.getTitle() != null && !sheetData.getTitle().isEmpty()) {
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheetData.getTitle());
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, sheetData.getHeaders().size() - 1));
            rowNum++; // Empty row
        }

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        List<String> headers = sheetData.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        for (Map<String, Object> rowData : sheetData.getData()) {
            Row dataRow = sheet.createRow(rowNum++);

            int colNum = 0;
            for (String header : headers) {
                Cell cell = dataRow.createCell(colNum++);
                Object value = rowData.get(header);

                setCellValue(cell, value, dataStyle, numberStyle, dateStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Set cell value with appropriate type and style
     */
    private void setCellValue(Cell cell, Object value, CellStyle dataStyle,
            CellStyle numberStyle, CellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                cell.setCellValue(((BigDecimal) value).doubleValue());
            } else {
                cell.setCellValue(((Number) value).doubleValue());
            }
            cell.setCellStyle(numberStyle);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).format(DATE_FORMATTER));
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DATETIME_FORMATTER));
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Boolean) {
            cell.setCellValue(((Boolean) value) ? "Yes" : "No");
            cell.setCellStyle(dataStyle);
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(dataStyle);
        }
    }

    /**
     * Create title style
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Create header style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create data cell style
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create number cell style
     */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create date cell style
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Helper class for sheet data
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SheetData {
        private String title;
        private List<String> headers;
        private List<Map<String, Object>> data;
    }
}
