package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.input.DataSource;
import com.diffmaster.rules.ComparisonRules;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.*;

/**
 * Excel comparison with multi-sheet support.
 */
public class ExcelComparator extends AbstractComparator {

    @Override
    public String getFormat() {
        return "excel";
    }

    @Override
    public boolean supports(String format) {
        return "excel".equalsIgnoreCase(format) ||
                "xlsx".equalsIgnoreCase(format) ||
                "xls".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        throw new ComparisonException("Excel comparison requires file/stream input, not string");
    }

    @Override
    public ComparisonResult compare(DataSource expected, DataSource actual, ComparisonRules rules) {
        reset();
        long startTime = System.currentTimeMillis();

        try (InputStream expStream = expected.getInputStream();
                InputStream actStream = actual.getInputStream();
                Workbook expWorkbook = new XSSFWorkbook(expStream);
                Workbook actWorkbook = new XSSFWorkbook(actStream)) {

            compareWorkbooks(expWorkbook, actWorkbook, rules);

            long duration = System.currentTimeMillis() - startTime;
            return buildResult(expected.getName(), actual.getName(), duration);
        } catch (Exception e) {
            throw new ComparisonException("Failed to compare Excel files: " + e.getMessage(), e);
        }
    }

    private void compareWorkbooks(Workbook expected, Workbook actual, ComparisonRules rules) {
        Set<String> allSheets = new LinkedHashSet<>();
        for (int i = 0; i < expected.getNumberOfSheets(); i++) {
            allSheets.add(expected.getSheetName(i));
        }
        for (int i = 0; i < actual.getNumberOfSheets(); i++) {
            allSheets.add(actual.getSheetName(i));
        }

        for (String sheetName : allSheets) {
            FieldPath sheetPath = FieldPath.of("sheet").child(sheetName);

            if (rules.shouldIgnore(sheetPath, null, null)) {
                continue;
            }

            Sheet expSheet = expected.getSheet(sheetName);
            Sheet actSheet = actual.getSheet(sheetName);

            if (expSheet == null) {
                recordAdded(sheetPath, "Sheet added: " + sheetName);
                continue;
            }

            if (actSheet == null) {
                recordRemoved(sheetPath, "Sheet removed: " + sheetName);
                continue;
            }

            compareSheets(expSheet, actSheet, sheetPath, rules);
        }
    }

    private void compareSheets(Sheet expected, Sheet actual, FieldPath sheetPath, ComparisonRules rules) {
        int maxRows = Math.max(getLastRowNum(expected), getLastRowNum(actual));

        for (int rowNum = 0; rowNum <= maxRows; rowNum++) {
            Row expRow = expected.getRow(rowNum);
            Row actRow = actual.getRow(rowNum);
            FieldPath rowPath = sheetPath.child("row").index(rowNum + 1);

            if (expRow == null && actRow == null)
                continue;

            if (expRow == null) {
                recordAdded(rowPath, rowToString(actRow));
                continue;
            }

            if (actRow == null) {
                recordRemoved(rowPath, rowToString(expRow));
                continue;
            }

            compareRows(expRow, actRow, rowPath, rules);
        }
    }

    private void compareRows(Row expected, Row actual, FieldPath rowPath, ComparisonRules rules) {
        int maxCols = Math.max(getLastCellNum(expected), getLastCellNum(actual));

        for (int colNum = 0; colNum < maxCols; colNum++) {
            Cell expCell = expected.getCell(colNum);
            Cell actCell = actual.getCell(colNum);
            FieldPath cellPath = rowPath.child(getColumnName(colNum));

            if (rules.shouldIgnore(cellPath, null, null)) {
                continue;
            }

            String expValue = getCellValue(expCell);
            String actValue = getCellValue(actCell);

            if (expValue == null && actValue == null)
                continue;

            if (expValue == null) {
                recordAdded(cellPath, actValue);
            } else if (actValue == null) {
                recordRemoved(cellPath, expValue);
            } else if (rules.areValuesEqual(expValue, actValue)) {
                recordMatch(cellPath, expValue);
            } else {
                recordModified(cellPath, expValue, actValue);
            }
        }
    }

    private int getLastRowNum(Sheet sheet) {
        return sheet.getLastRowNum();
    }

    private int getLastCellNum(Row row) {
        return row.getLastCellNum();
    }

    private String getCellValue(Cell cell) {
        if (cell == null)
            return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }

    private String rowToString(Row row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (i > 0)
                sb.append(", ");
            String value = getCellValue(row.getCell(i));
            sb.append(value != null ? value : "");
        }
        return sb.toString();
    }

    private String getColumnName(int colNum) {
        StringBuilder sb = new StringBuilder();
        colNum++;
        while (colNum > 0) {
            colNum--;
            sb.insert(0, (char) ('A' + colNum % 26));
            colNum /= 26;
        }
        return sb.toString();
    }
}
