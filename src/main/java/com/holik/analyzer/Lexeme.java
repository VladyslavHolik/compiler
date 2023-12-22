package com.holik.analyzer;

import java.util.regex.Pattern;

public enum Lexeme {
    OPENING_BRACKET(Pattern.compile("^\\s*\\(\\s*")),
    CLOSING_BRACKET(Pattern.compile("^\\s*\\)\\s*")),
    PLUS(Pattern.compile("^\\s*\\+\\s*")),
    MINUS(Pattern.compile("^\\s*-\\s*")),
    MULTIPLY(Pattern.compile("^\\s*\\*\\s*")),
    DIVIDE(Pattern.compile("^\\s*/\\s*")),
    COMMA(Pattern.compile("^\\s*,\\s*")),
    CONSTANT(Pattern.compile("^\\s*\\d*\\.?\\d+\\s*")),
    FUNCTION_NAME(Pattern.compile("^\\s*[^0-9()+\\-*/,^][^()+\\-*/,^\\s]*\\(")),
    VARIABLE(Pattern.compile("^\\s*[^0-9()+\\-*/,^][^()+\\-*/,^\\s]*")),
    START(null),
    END(null);

    private final Pattern pattern;

    Lexeme(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
