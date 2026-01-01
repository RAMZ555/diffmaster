# DiffMaster - Universal Data Comparator Library

**DiffMaster** is a powerful, production-ready Java library for comparing data files with intelligent rules and beautiful reports. It supports JSON, XML, CSV, Excel, YAML, Properties, and Text formats.

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

## Features

- 🔄 **Universal Support**: Compare JSON, XML, CSV, Excel (XLSX), YAML, Properties, and Text.
- 🧠 **Smart Rules**: Ignore fields, tolerance (numeric/date), case sensitivity, array order independence.
- 📊 **Beautiful Reports**: Generate interactive HTML reports (with Dark Mode), JSON, or Markdown.
- 🎯 **Deep Comparison**: Full recursive comparison with detailed path tracking (e.g., `users[0].address.city`).
- 🛠️ **Fluent API**: Intuitive, readable API design for easy integration.
- 🧪 **Test Integration**: Built-in Fluent Assertions for JUnit/TestNG.

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.diffmaster</groupId>
    <artifactId>diffmaster</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Simple File Comparison

```java
import com.diffmaster.DiffMaster;

DiffMaster.compare("expected.json", "actual.json")
    .printSummary();
```

### 2. Advanced Comparison with Rules

```java
import com.diffmaster.DiffMaster;

boolean match = DiffMaster.compare(user1, user2)
    .ignoreFields("id", "updatedAt")      // Ignore specific fields
    .ignoreArrayOrder()                   // [A, B] == [B, A]
    .numericTolerance(0.01)               // 10.00 == 10.009
    .caseInsensitive()                    // "Admin" == "admin"
    .isMatch();
```

### 3. Generate HTML Report

```java
DiffMaster.compare(expected, actual)
    .asHtml()
    .withTitle("API Regression Test")
    .withDarkMode()
    .saveTo("diff-report.html");
```

## Supported Formats & Features

| Format | Features |
|--------|----------|
| **JSON** | Deep comparison, ignore fields, fuzzy matching |
| **XML** | Namespace support, attribute comparison, unordered children |
| **CSV** | Header support, unordered rows, column ignoring |
| **Excel**| Multi-sheet support, formula handling, row/cell comparison |
| **YAML** | Full tree comparison (converted to JSON structure) |
| **Text** | Line-by-line comparison, whitespace normalization |

## Comparison Rules

| Rule | Method | Description |
|------|--------|-------------|
| **Ignore Field** | `.ignoreFields("id")` | Skip fields by name |
| **Pattern** | `.ignorePattern(".*_at")` | Skip fields by Regex |
| **Array Order** | `.ignoreArrayOrder()` | Treat arrays as sets |
| **Numeric** | `.numericTolerance(0.1)` | Allow small number differences |
| **Date/Time** | `.dateToleranceSeconds(5)` | Allow time skew |
| **Case** | `.caseInsensitive()` | Ignore String case |
| **Coercion** | `.typeCoercion()` | "123" == 123 |
| **Whitespace**| `.ignoreWhitespace()` | Trim and collapse spaces |

## Testing Integration (JUnit 5)

Use `DiffMasterAssertions` for fluent test assertions:

```java
import static com.diffmaster.assertion.DiffMasterAssertions.assertThat;

@Test
void testApi() {
    String jsonResponse = api.call();
    
    assertThat(jsonResponse)
        .ignoreFields("traceId")
        .ignoreArrayOrder()
        .isEqualTo(expectedJson);
}
```

## Building from Source

```bash
# Windows (CMD)
mvnw clean install

# Mac/Linux/PowerShell
./mvnw clean install

# Run tests
mvnw test
```

## License

MIT License - see [LICENSE](LICENSE) for details.
