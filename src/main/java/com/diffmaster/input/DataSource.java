package com.diffmaster.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Abstract data source for comparison input.
 */
public interface DataSource {
    /**
     * Get the input stream for this source.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Get the content as a string.
     */
    String getContent() throws IOException;

    /**
     * Get a name/identifier for this source.
     */
    String getName();

    /**
     * Get the format hint (e.g., "json", "xml", "csv").
     * Returns null if unknown.
     */
    String getFormatHint();

    /**
     * Get the file path if this source is file-based.
     * Returns null if not applicable.
     */
    default Path getPath() {
        return null;
    }

    /**
     * Get content safely (returns error message if failed).
     */
    default String getContentSafe() {
        try {
            return getContent();
        } catch (IOException e) {
            return "Failed to read content: " + e.getMessage();
        }
    }
}
