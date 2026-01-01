package com.diffmaster.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a path to a field in JSON/XML/nested structures.
 * Examples: "user.name", "items[0].price", "root.users[2].email"
 */
@Getter
@EqualsAndHashCode
public class FieldPath {
    private final List<String> segments;

    private FieldPath(List<String> segments) {
        this.segments = new ArrayList<>(segments);
    }

    /**
     * Create a root path (empty).
     */
    public static FieldPath root() {
        return new FieldPath(new ArrayList<>());
    }

    /**
     * Create path from dot-notation string.
     * Example: "user.profile.name" or "items[0].price"
     */
    public static FieldPath of(String path) {
        if (path == null || path.isBlank()) {
            return root();
        }
        List<String> segments = Arrays.asList(path.split("\\."));
        return new FieldPath(segments);
    }

    /**
     * Create path from segments.
     */
    public static FieldPath of(String... segments) {
        return new FieldPath(Arrays.asList(segments));
    }

    /**
     * Append a child field to this path.
     */
    public FieldPath child(String fieldName) {
        List<String> newSegments = new ArrayList<>(segments);
        newSegments.add(fieldName);
        return new FieldPath(newSegments);
    }

    /**
     * Append an array index to this path.
     */
    public FieldPath index(int index) {
        List<String> newSegments = new ArrayList<>(segments);
        if (newSegments.isEmpty()) {
            newSegments.add("[" + index + "]");
        } else {
            int lastIdx = newSegments.size() - 1;
            newSegments.set(lastIdx, newSegments.get(lastIdx) + "[" + index + "]");
        }
        return new FieldPath(newSegments);
    }

    /**
     * Get the parent path.
     */
    public FieldPath parent() {
        if (segments.isEmpty()) {
            return this;
        }
        return new FieldPath(segments.subList(0, segments.size() - 1));
    }

    /**
     * Check if this path is empty (root).
     */
    public boolean isRoot() {
        return segments.isEmpty();
    }

    /**
     * Get the last segment (field name).
     */
    public String getLastSegment() {
        return segments.isEmpty() ? "" : segments.get(segments.size() - 1);
    }

    /**
     * Get depth of the path.
     */
    public int depth() {
        return segments.size();
    }

    /**
     * Check if this path starts with another path.
     */
    public boolean startsWith(FieldPath other) {
        if (other.segments.size() > this.segments.size()) {
            return false;
        }
        for (int i = 0; i < other.segments.size(); i++) {
            if (!this.segments.get(i).equals(other.segments.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if path matches a pattern (supports * wildcard).
     */
    public boolean matches(String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("[", "\\[")
                .replace("]", "\\]");
        return toString().matches(regex);
    }

    @Override
    public String toString() {
        if (segments.isEmpty()) {
            return "$";
        }
        return segments.stream()
                .collect(Collectors.joining("."));
    }

    /**
     * Get a display-friendly representation.
     */
    public String toDisplayString() {
        if (segments.isEmpty()) {
            return "(root)";
        }
        return toString();
    }
}
