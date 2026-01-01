package com.diffmaster.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Data source from a string.
 */
public class StringSource implements DataSource {
    private final String content;
    private final String name;
    private final String formatHint;

    public StringSource(String content) {
        this(content, "inline", null);
    }

    public StringSource(String content, String formatHint) {
        this(content, "inline", formatHint);
    }

    public StringSource(String content, String name, String formatHint) {
        this.content = content;
        this.name = name;
        this.formatHint = formatHint;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormatHint() {
        if (formatHint != null)
            return formatHint;
        // Try to detect from content
        String trimmed = content.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("["))
            return "json";
        if (trimmed.startsWith("<"))
            return "xml";
        return null;
    }
}
