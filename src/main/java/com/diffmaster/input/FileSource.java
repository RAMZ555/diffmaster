package com.diffmaster.input;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data source from a file path.
 */
public class FileSource implements DataSource {
    private final Path path;
    private String cachedContent;

    public FileSource(Path path) {
        this.path = path;
    }

    public FileSource(String path) {
        this.path = Path.of(path);
    }

    public FileSource(File file) {
        this.path = file.toPath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public String getContent() throws IOException {
        if (cachedContent == null) {
            cachedContent = Files.readString(path, StandardCharsets.UTF_8);
        }
        return cachedContent;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getFormatHint() {
        String name = getName().toLowerCase();
        if (name.endsWith(".json"))
            return "json";
        if (name.endsWith(".xml"))
            return "xml";
        if (name.endsWith(".csv"))
            return "csv";
        if (name.endsWith(".xlsx") || name.endsWith(".xls"))
            return "excel";
        if (name.endsWith(".yaml") || name.endsWith(".yml"))
            return "yaml";
        if (name.endsWith(".properties"))
            return "properties";
        if (name.endsWith(".txt"))
            return "text";
        return null;
    }

    @Override
    public Path getPath() {
        return path;
    }
}
