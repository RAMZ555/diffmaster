package com.diffmaster.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Data source from a URL.
 */
public class UrlSource implements DataSource {
    private final URL url;
    private final String formatHint;
    private String cachedContent;

    public UrlSource(URL url) {
        this(url, null);
    }

    public UrlSource(URL url, String formatHint) {
        this.url = url;
        this.formatHint = formatHint;
    }

    public UrlSource(String urlString) throws IOException {
        this(new URL(urlString), null);
    }

    public UrlSource(String urlString, String formatHint) throws IOException {
        this(new URL(urlString), formatHint);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return url.openStream();
    }

    @Override
    public String getContent() throws IOException {
        if (cachedContent == null) {
            try (InputStream is = url.openStream()) {
                cachedContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return cachedContent;
    }

    @Override
    public String getName() {
        return url.toString();
    }

    @Override
    public String getFormatHint() {
        if (formatHint != null)
            return formatHint;
        String path = url.getPath().toLowerCase();
        if (path.endsWith(".json"))
            return "json";
        if (path.endsWith(".xml"))
            return "xml";
        if (path.endsWith(".csv"))
            return "csv";
        if (path.endsWith(".yaml") || path.endsWith(".yml"))
            return "yaml";
        return null;
    }
}
