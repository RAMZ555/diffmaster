package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Rule to ignore fields matching regex patterns.
 */
@Getter
public class IgnorePatternRule implements Rule {
    private final List<Pattern> patterns;

    public IgnorePatternRule(String... regexPatterns) {
        this.patterns = new ArrayList<>();
        for (String regex : regexPatterns) {
            patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
    }

    @Override
    public String getName() {
        return "IgnorePattern";
    }

    @Override
    public boolean appliesTo(FieldPath path) {
        if (path == null)
            return false;
        String pathStr = path.toString();
        return patterns.stream().anyMatch(p -> p.matcher(pathStr).matches());
    }

    @Override
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        return appliesTo(path);
    }
}
