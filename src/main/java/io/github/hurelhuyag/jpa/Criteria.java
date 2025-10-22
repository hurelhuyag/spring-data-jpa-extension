package io.github.hurelhuyag.jpa;

import java.util.regex.Pattern;

/**
 * Filter Crieria
 * @param attr entity attribute path
 * @param expr expession
 * @param arg argument which type is matched to attr type
 */
public record Criteria(String attr, Expr expr, Object arg) {

    private static final Pattern PATTERN_ATTR = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*");

    public Criteria {
        if (!PATTERN_ATTR.matcher(attr).matches()) {
            throw new IllegalArgumentException("Invalid attribute name: " + attr);
        }
    }
}