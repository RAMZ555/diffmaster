package com.diffmaster.report;

import com.diffmaster.core.*;
import com.diffmaster.exception.ReportGenerationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Beautiful HTML report generator with dark mode support.
 */
public class HtmlReportGenerator implements ReportGenerator {
  private final String title;
  private final boolean darkMode;
  private final boolean showMatchedFields;

  public HtmlReportGenerator() {
    this("DiffMaster Comparison Report", false, false);
  }

  public HtmlReportGenerator(String title, boolean darkMode, boolean showMatchedFields) {
    this.title = title;
    this.darkMode = darkMode;
    this.showMatchedFields = showMatchedFields;
  }

  @Override
  public void generate(ComparisonResult result, String expectedName, String actualName, Path outputPath) {
    String html = generateString(result, expectedName, actualName);
    try {
      Files.writeString(outputPath, html);
    } catch (IOException e) {
      throw new ReportGenerationException("Failed to write HTML report to " + outputPath, e);
    }
  }

  @Override
  public String generateString(ComparisonResult result, String expectedName, String actualName) {
    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE html>\n");
    html.append("<html lang=\"en\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"UTF-8\">\n");
    html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
    html.append("  <title>").append(escapeHtml(title)).append("</title>\n");
    html.append(generateStyles());
    html.append("</head>\n");
    html.append("<body class=\"").append(darkMode ? "dark" : "light").append("\">\n");

    // Header
    html.append(generateHeader(result, expectedName, actualName));

    // Summary cards
    html.append(generateSummaryCards(result.getSummary()));

    // Filters and controls
    html.append(generateControls());

    // Differences table
    html.append(generateDifferencesTable(result));

    // Footer
    html.append(generateFooter());

    // JavaScript
    html.append(generateScripts());

    html.append("</body>\n");
    html.append("</html>\n");

    return html.toString();
  }

  private String generateStyles() {
    return """
        <style>
          :root {
            --bg-primary: #ffffff;
            --bg-secondary: #f8f9fa;
            --bg-card: #ffffff;
            --text-primary: #212529;
            --text-secondary: #6c757d;
            --border-color: #dee2e6;
            --success-color: #28a745;
            --success-bg: #d4edda;
            --danger-color: #dc3545;
            --danger-bg: #f8d7da;
            --warning-color: #ffc107;
            --warning-bg: #fff3cd;
            --info-color: #17a2b8;
            --info-bg: #d1ecf1;
            --shadow: 0 2px 8px rgba(0,0,0,0.1);
          }

          .dark {
            --bg-primary: #1a1a2e;
            --bg-secondary: #16213e;
            --bg-card: #0f3460;
            --text-primary: #eaeaea;
            --text-secondary: #a0a0a0;
            --border-color: #3a3a5c;
            --success-bg: #1e4620;
            --danger-bg: #4a1f1f;
            --warning-bg: #4a3f1f;
            --info-bg: #1f3a4a;
            --shadow: 0 2px 8px rgba(0,0,0,0.3);
          }

          * { box-sizing: border-box; margin: 0; padding: 0; }

          body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            line-height: 1.6;
            padding: 20px;
          }

          .container { max-width: 1400px; margin: 0 auto; }

          .header {
            text-align: center;
            padding: 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 12px;
            margin-bottom: 24px;
            box-shadow: var(--shadow);
          }

          .header h1 { font-size: 2rem; margin-bottom: 8px; }
          .header .meta { opacity: 0.9; font-size: 0.9rem; }

          .summary-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
          }

          .card {
            background: var(--bg-card);
            border-radius: 12px;
            padding: 20px;
            text-align: center;
            box-shadow: var(--shadow);
            border: 1px solid var(--border-color);
            transition: transform 0.2s;
          }

          .card:hover { transform: translateY(-2px); }
          .card .value { font-size: 2.5rem; font-weight: bold; }
          .card .label { color: var(--text-secondary); font-size: 0.85rem; text-transform: uppercase; }
          .card.success .value { color: var(--success-color); }
          .card.danger .value { color: var(--danger-color); }
          .card.warning .value { color: var(--warning-color); }
          .card.info .value { color: var(--info-color); }

          .controls {
            display: flex;
            gap: 12px;
            margin-bottom: 20px;
            flex-wrap: wrap;
            align-items: center;
          }

          .control-group { display: flex; gap: 8px; align-items: center; }

          button {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9rem;
            transition: all 0.2s;
            background: var(--bg-secondary);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
          }

          button:hover { background: var(--info-color); color: white; }
          button.active { background: var(--info-color); color: white; }

          .search-box {
            flex: 1;
            min-width: 200px;
            padding: 10px 16px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            font-size: 0.9rem;
            background: var(--bg-card);
            color: var(--text-primary);
          }

          .diff-table {
            width: 100%;
            border-collapse: collapse;
            background: var(--bg-card);
            border-radius: 12px;
            overflow: hidden;
            box-shadow: var(--shadow);
          }

          .diff-table th {
            background: var(--bg-secondary);
            padding: 14px 16px;
            text-align: left;
            font-weight: 600;
            border-bottom: 2px solid var(--border-color);
          }

          .diff-table td {
            padding: 12px 16px;
            border-bottom: 1px solid var(--border-color);
            vertical-align: top;
          }

          .diff-table tr:last-child td { border-bottom: none; }
          .diff-table tr:hover { background: var(--bg-secondary); }

          .path { font-family: 'Monaco', 'Consolas', monospace; font-size: 0.85rem; color: var(--info-color); }

          .type-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
          }

          .type-MODIFIED { background: var(--warning-bg); color: var(--warning-color); }
          .type-ADDED { background: var(--success-bg); color: var(--success-color); }
          .type-REMOVED { background: var(--danger-bg); color: var(--danger-color); }
          .type-MATCH { background: var(--info-bg); color: var(--info-color); }

          .value-cell {
            font-family: 'Monaco', 'Consolas', monospace;
            font-size: 0.85rem;
            word-break: break-all;
            max-width: 300px;
          }

          .value-expected { color: var(--danger-color); }
          .value-actual { color: var(--success-color); }

          .footer {
            text-align: center;
            padding: 20px;
            color: var(--text-secondary);
            font-size: 0.85rem;
            margin-top: 30px;
          }

          .no-diff {
            text-align: center;
            padding: 60px;
            color: var(--success-color);
          }

          .no-diff svg { width: 80px; height: 80px; margin-bottom: 16px; }

          @media (max-width: 768px) {
            .header h1 { font-size: 1.5rem; }
            .summary-cards { grid-template-columns: repeat(2, 1fr); }
            .controls { flex-direction: column; }
            .diff-table { font-size: 0.85rem; }
          }
        </style>
        """;
  }

  private String generateHeader(ComparisonResult result, String expectedName, String actualName) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String statusIcon = result.isMatch() ? "✓" : "✗";

    return String.format("""
        <div class="container">
          <div class="header">
            <h1>%s %s</h1>
            <div class="meta">
              <strong>Expected:</strong> %s &nbsp;|&nbsp;
              <strong>Actual:</strong> %s &nbsp;|&nbsp;
              <strong>Generated:</strong> %s
            </div>
          </div>
        """, statusIcon, escapeHtml(title), escapeHtml(expectedName),
        escapeHtml(actualName), timestamp);
  }

  private String generateSummaryCards(ComparisonSummary summary) {
    return String.format("""
          <div class="summary-cards">
            <div class="card info">
              <div class="value">%d</div>
              <div class="label">Total Fields</div>
            </div>
            <div class="card success">
              <div class="value">%d</div>
              <div class="label">Matched</div>
            </div>
            <div class="card warning">
              <div class="value">%d</div>
              <div class="label">Modified</div>
            </div>
            <div class="card danger">
              <div class="value">%d</div>
              <div class="label">Added / Removed</div>
            </div>
            <div class="card">
              <div class="value">%.1f%%</div>
              <div class="label">Match Rate</div>
            </div>
          </div>
        """, summary.getTotalFields(), summary.getMatchCount(),
        summary.getModifiedCount(), summary.getAddedCount() + summary.getRemovedCount(),
        summary.getMatchPercentage());
  }

  private String generateControls() {
    return """
          <div class="controls">
            <div class="control-group">
              <button id="btnAll" class="active" onclick="filterDiffs('all')">All</button>
              <button id="btnDiffs" onclick="filterDiffs('diff')">Differences Only</button>
              <button id="btnModified" onclick="filterDiffs('MODIFIED')">Modified</button>
              <button id="btnAdded" onclick="filterDiffs('ADDED')">Added</button>
              <button id="btnRemoved" onclick="filterDiffs('REMOVED')">Removed</button>
            </div>
            <input type="text" class="search-box" id="searchBox" placeholder="Search paths or values..." oninput="searchDiffs()">
            <button onclick="jumpToNext()">Next Diff ↓</button>
          </div>
        """;
  }

  private String generateDifferencesTable(ComparisonResult result) {
    StringBuilder html = new StringBuilder();

    List<Difference> diffs = showMatchedFields ? result.getDifferences() : result.getOnlyDifferences();

    if (diffs.isEmpty()) {
      html.append("""
            <div class="no-diff">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <h2>Perfect Match!</h2>
              <p>No differences found between the files.</p>
            </div>
          """);
    } else {
      html.append("""
            <table class="diff-table" id="diffTable">
              <thead>
                <tr>
                  <th style="width:50px">#</th>
                  <th>Path</th>
                  <th style="width:100px">Type</th>
                  <th>Expected</th>
                  <th>Actual</th>
                </tr>
              </thead>
              <tbody>
          """);

      int index = 1;
      for (Difference diff : diffs) {
        String rowClass = diff.isDifferent() ? "diff-row" : "match-row";
        html.append(String.format("""
              <tr class="%s" data-type="%s">
                <td>%d</td>
                <td class="path">%s</td>
                <td><span class="type-badge type-%s">%s</span></td>
                <td class="value-cell value-expected">%s</td>
                <td class="value-cell value-actual">%s</td>
              </tr>
            """, rowClass, diff.getType().name(), index++,
            escapeHtml(diff.getPathString()),
            diff.getType().name(), diff.getType().name(),
            escapeHtml(diff.getExpectedAsString()),
            escapeHtml(diff.getActualAsString())));
      }

      html.append("</tbody></table>\n");
    }

    return html.toString();
  }

  private String generateFooter() {
    return """
            <div class="footer">
              Generated by <strong>DiffMaster</strong> - Universal Data Comparator Library
            </div>
          </div>
        """;
  }

  private String generateScripts() {
    return """
        <script>
          let currentDiffIndex = -1;

          function filterDiffs(type) {
            document.querySelectorAll('.controls button').forEach(b => b.classList.remove('active'));
            event.target.classList.add('active');

            const rows = document.querySelectorAll('#diffTable tbody tr');
            rows.forEach(row => {
              if (type === 'all') {
                row.style.display = '';
              } else if (type === 'diff') {
                row.style.display = row.classList.contains('match-row') ? 'none' : '';
              } else {
                row.style.display = row.dataset.type === type ? '' : 'none';
              }
            });
          }

          function searchDiffs() {
            const query = document.getElementById('searchBox').value.toLowerCase();
            const rows = document.querySelectorAll('#diffTable tbody tr');
            rows.forEach(row => {
              const text = row.textContent.toLowerCase();
              row.style.display = text.includes(query) ? '' : 'none';
            });
          }

          function jumpToNext() {
            const visibleRows = [...document.querySelectorAll('#diffTable tbody tr.diff-row')]
              .filter(r => r.style.display !== 'none');
            if (visibleRows.length === 0) return;

            currentDiffIndex = (currentDiffIndex + 1) % visibleRows.length;
            visibleRows[currentDiffIndex].scrollIntoView({ behavior: 'smooth', block: 'center' });
            visibleRows[currentDiffIndex].style.background = 'var(--warning-bg)';
            setTimeout(() => {
              visibleRows[currentDiffIndex].style.background = '';
            }, 1500);
          }

          // Theme toggle
          document.addEventListener('keydown', e => {
            if (e.key === 't' && e.ctrlKey) {
              document.body.classList.toggle('dark');
              document.body.classList.toggle('light');
            }
          });
        </script>
        """;
  }

  private String escapeHtml(String text) {
    if (text == null)
      return "null";
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
