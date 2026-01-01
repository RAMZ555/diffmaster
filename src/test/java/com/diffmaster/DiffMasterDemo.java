package com.diffmaster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * End-to-End demonstration of DiffMaster library.
 */
public class DiffMasterDemo {

    public static void main(String[] args) throws IOException {
        System.out.println("Running DiffMaster Demo...");
        Files.createDirectories(Path.of("demo-output"));

        runJsonDemo();
        runCsvDemo();
        runXmlDemo();

        System.out.println("Demo completed! Check 'demo-output' directory.");
    }

    private static void runJsonDemo() throws IOException {
        System.out.println("\n--- JSON Comparison ---");

        String expected = """
                {
                  "id": 101,
                  "name": "Alice",
                  "roles": ["ADMIN", "USER"],
                  "meta": { "lastLogin": "2023-01-01T10:00:00Z" }
                }
                """;

        String actual = """
                {
                  "id": 101,
                  "name": "alice",
                  "roles": ["USER", "ADMIN"],
                  "meta": { "lastLogin": "2023-01-01T10:00:05Z", "ip": "127.0.0.1" }
                }
                """;

        System.out.println("Comparing JSON...");
        DiffMaster.compareStrings(expected, actual)
                .caseInsensitive()
                .ignoreArrayOrder() // Should handle roles order
                .dateToleranceSeconds(10) // Should handle lastLogin diff
                .ignoreFields("ip") // Should ignore extra field
                .printSummary();

        // Generate Report
        DiffMaster.compareStrings(expected, actual)
                .caseInsensitive()
                .ignoreArrayOrder()
                .asHtml()
                .withTitle("JSON Demo Report")
                .saveTo("demo-output/json-report.html");

        System.out.println("Saved demo-output/json-report.html");
    }

    private static void runCsvDemo() throws IOException {
        System.out.println("\n--- CSV Comparison ---");

        String expected = """
                id,name,score
                1,Alice,100
                2,Bob,90
                3,Charlie,85
                """;

        String actual = """
                id,name,score
                3,Charlie,85
                1,Alice,100
                2,Bob,92
                """;

        System.out.println("Comparing CSV (Unordered)...");
        DiffMaster.compareStrings(expected, actual)
                .asCsv()
                .ignoreArrayOrder() // Row order doesn't matter
                .printSummary();

        System.out.println("Comparing CSV (Strict)...");
        DiffMaster.compareStrings(expected, actual)
                .asCsv()
                .printSummary();

        DiffMaster.compareStrings(expected, actual)
                .asCsv()
                .asHtml()
                .withTitle("CSV Demo Report")
                .saveTo("demo-output/csv-report.html");
    }

    private static void runXmlDemo() throws IOException {
        System.out.println("\n--- XML Comparison ---");

        String expected = """
                <users>
                    <user id="1"><name>John</name></user>
                    <user id="2"><name>Jane</name></user>
                </users>
                """;

        String actual = """
                <users>
                    <user id="2"><name>Jane</name></user>
                    <user id="1"><name>John Doe</name></user>
                </users>
                """;

        System.out.println("Comparing XML...");
        DiffMaster.compareStrings(expected, actual)
                .asXml()
                .ignoreArrayOrder()
                .printSummary();

        DiffMaster.compareStrings(expected, actual)
                .asXml()
                .asHtml()
                .withTitle("XML Demo Report")
                .withDarkMode()
                .saveTo("demo-output/xml-report.html");
    }
}
