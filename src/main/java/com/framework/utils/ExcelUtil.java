package com.framework.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Apache POI wrapper for reading and writing XLSX test-data files.
 *
 * <pre>
 *   // Read all rows from "Sheet1"
 *   List&lt;Map&lt;String,String&gt;&gt; data = ExcelUtil.readSheet("testdata/Login.xlsx", "Sheet1");
 *
 *   // Write results
 *   ExcelUtil.writeCellValue("report.xlsx", "Results", 1, 2, "PASS");
 * </pre>
 */
public final class ExcelUtil {

    private ExcelUtil() {}

    /**
     * Reads all rows from the given sheet into a list of header→value maps.
     * Row 0 is assumed to be the header row.
     */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            Row header = sheet.getRow(0);
            int colCount = header.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < colCount; c++) {
                headers.add(getCellValue(header.getCell(c)));
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < colCount; c++) {
                    rowMap.put(headers.get(c), getCellValue(row.getCell(c)));
                }
                data.add(rowMap);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
        return data;
    }

    /**
     * Writes a single string value to the given row/column (0-based) in an existing file.
     */
    public static void writeCellValue(String filePath, String sheetName,
                                      int rowIndex, int colIndex, String value) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) sheet = wb.createSheet(sheetName);
            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);
            Cell cell = row.getCell(colIndex);
            if (cell == null) cell = row.createCell(colIndex);
            cell.setCellValue(value);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel file: " + filePath, e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter fmt = new DataFormatter();
        return fmt.formatCellValue(cell).trim();
    }
}
