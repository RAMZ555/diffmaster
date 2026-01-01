package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Rule to ignore specific field names.
 */
@Getter
public class IgnoreFieldRule implements Rule {
    private final Set<String> fieldNames;

    public IgnoreFieldRule(String... fields) {
        this.fieldNames = new HashSet<>();
        for (String field : fields) {
            fieldNames.add(field.toLowerCase());
        }
    }

    @Override
    public String getName() {
        return "IgnoreField";
    }

    @Override
    public boolean appliesTo(FieldPath path) {
        if (path == null || path.isRoot())
            return false;
        String lastSegment = path.getLastSegment().toLowerCase();
        // Remove array indices for matching
        lastSegment = lastSegment.replaceAll("\\[\\d+\\]", "");
        return fieldNames.contains(lastSegment);
    }

    @Override
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        return appliesTo(path);
    }
}
