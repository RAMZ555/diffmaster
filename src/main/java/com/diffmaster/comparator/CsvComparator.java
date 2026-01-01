package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.rules.ComparisonRules;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.StringReader;
import java.util.*;

/**
 * CSV comparison with header and row handling.
 */
public class CsvComparator extends AbstractComparator {
    private boolean hasHeader = true;
    private char delimiter = ',';

    public CsvComparator() {
    }

    public CsvComparator(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public CsvComparator(boolean hasHeader, char delimiter) {
        this.hasHeader = hasHeader;
        this.delimiter = delimiter;
    }

    @Override
    public String getFormat() {
        return "csv";
    }

    @Override
    public boolean supports(String format) {
        return "csv".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        reset();
        long startTime = System.currentTimeMillis();

        try {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(delimiter)
                    .setHeader()
                    .setSkipHeaderRecord(hasHeader)
                    .build();

            List<CSVRecord> expRecords = CSVParser.parse(new StringReader(expected), format).getRecords();
            List<CSVRecord> actRecords = CSVParser.parse(new StringReader(actual), format).getRecords();

            // Get headers
            List<String> headers = new ArrayList<>();
            if (hasHeader && !expRecords.isEmpty()) {
                headers.addAll(expRecords.get(0).getParser().getHeaderNames());
            }

            if (rules.isIgnoreArrayOrder()) {
                compareRowsUnordered(expRecords, actRecords, headers, rules);
            } else {
                compareRowsOrdered(expRecords, actRecords, headers, rules);
            }

            long duration = System.currentTimeMillis() - startTime;
            return buildResult("expected.csv", "actual.csv", duration);
        } catch (Exception e) {
            throw new ComparisonException("Failed to parse CSV: " + e.getMessage(), e);
        }
    }

    private void compareRowsOrdered(List<CSVRecord> expected, List<CSVRecord> actual,
            List<String> headers, ComparisonRules rules) {
        int maxRows = Math.max(expected.size(), actual.size());

        for (int row = 0; row < maxRows; row++) {
            FieldPath rowPath = FieldPath.of("row").index(row + 1);

            if (row >= expected.size()) {
                recordAdded(rowPath, recordToString(actual.get(row)));
                continue;
            }

            if (row >= actual.size()) {
                recordRemoved(rowPath, recordToString(expected.get(row)));
                continue;
            }

            compareRecords(expected.get(row), actual.get(row), rowPath, headers, rules);
        }
    }

    private void compareRowsUnordered(List<CSVRecord> expected, List<CSVRecord> actual,
            List<String> headers, ComparisonRules rules) {
        boolean[] matchedActual = new boolean[actual.size()];

        for (int i = 0; i < expected.size(); i++) {
            CSVRecord expRecord = expected.get(i);
            FieldPath rowPath = FieldPath.of("row").index(i + 1);
            boolean found = false;

            for (int j = 0; j < actual.size(); j++) {
                if (!matchedActual[j] && recordsEqual(expRecord, actual.get(j), rules)) {
                    matchedActual[j] = true;
                    found = true;
                    recordMatch(rowPath, recordToString(expRecord));
                    break;
                }
            }

            if (!found) {
                recordRemoved(rowPath, recordToString(expRecord));
            }
        }

        for (int j = 0; j < actual.size(); j++) {
            if (!matchedActual[j]) {
                FieldPath rowPath = FieldPath.of("row").index(j + 1);
                recordAdded(rowPath, recordToString(actual.get(j)));
            }
        }
    }

    private void compareRecords(CSVRecord expected, CSVRecord actual, FieldPath rowPath,
            List<String> headers, ComparisonRules rules) {
        int maxCols = Math.max(expected.size(), actual.size());

        for (int col = 0; col < maxCols; col++) {
            String colName = col < headers.size() ? headers.get(col) : "col" + (col + 1);
            FieldPath colPath = rowPath.child(colName);

            if (rules.shouldIgnore(colPath, null, null)) {
                continue;
            }

            String expValue = col < expected.size() ? expected.get(col) : null;
            String actValue = col < actual.size() ? actual.get(col) : null;

            if (expValue == null) {
                recordAdded(colPath, actValue);
            } else if (actValue == null) {
                recordRemoved(colPath, expValue);
            } else if (rules.areValuesEqual(expValue, actValue)) {
                recordMatch(colPath, expValue);
            } else {
                recordModified(colPath, expValue, actValue);
            }
        }
    }

    private boolean recordsEqual(CSVRecord a, CSVRecord b, ComparisonRules rules) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!rules.areValuesEqual(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    private String recordToString(CSVRecord record) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < record.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(record.get(i));
        }
        return sb.toString();
    }
}
