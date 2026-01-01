package com.diffmaster.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Data source from an InputStream.
 */
public class StreamSource implements DataSource {
    private final String name;
    private final String formatHint;
    private String cachedContent;
    private byte[] cachedBytes;

    public StreamSource(InputStream inputStream, String formatHint) throws IOException {
        this("stream", inputStream, formatHint);
    }

    public StreamSource(String name, InputStream inputStream, String formatHint) throws IOException {
        this.name = name;
        this.formatHint = formatHint;
        // Read and cache since InputStream can only be read once
        this.cachedBytes = inputStream.readAllBytes();
        this.cachedContent = new String(cachedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(cachedBytes);
    }

    @Override
    public String getContent() {
        return cachedContent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormatHint() {
        return formatHint;
    }
}
