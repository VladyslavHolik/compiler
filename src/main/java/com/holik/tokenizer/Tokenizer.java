package com.holik.tokenizer;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Tokenizer {
    private int cursor = 0;
    private int lexemeIndex = 0;

    public Lexemes tokenize(String expression) {
        List<LexemeValue> lexemes = new ArrayList<>();
        lexemes.add(new LexemeValue(Lexeme.START, "", 0, 0));

        List<ParseError> errors = new ArrayList<>(checkForReservedKeywords(expression));

        while (cursor < expression.length()) {
            var lexemeFound = false;

            for (Lexeme lexeme : Lexeme.values()) {
                String subExpressionStr = expression.substring(cursor);
                if (lexeme.getPattern() == null) {
                    continue;
                }
                Matcher matcher = lexeme.getPattern().matcher(subExpressionStr);
                if (!matcher.find()) {
                    continue;
                }
                String value = matcher.group().replaceAll("\\s+", "");
                if (lexeme == Lexeme.FUNCTION_NAME) {
                    value = value.substring(0, value.length() - 1);
                }
                lexemes.add(new LexemeValue(lexeme, value, lexemeIndex, cursor));
                cursor += matcher.end();
                if (lexeme == Lexeme.FUNCTION_NAME) {
                    cursor -= 1;
                }
                lexemeIndex += 1;
                lexemeFound = true;
                break;
            }

            if (!lexemeFound) {
                errors.add(new ParseError(cursor, "Unexpected symbol '" + expression.charAt(cursor) + "'"));
                cursor += 1;
            }
        }

        lexemes.add(new LexemeValue(Lexeme.END, "", -1, -1));

        return new Lexemes(lexemes, errors);
    }

    public List<ParseError> checkForReservedKeywords(String expression) {
        List<ParseError> errors = new ArrayList<>();
        List<String> keywords = List.of(
                "abstract",
                "assert",
                "boolean",
                "break",
                "byte",
                "case",
                "catch",
                "char",
                "class",
                "const",
                "continue",
                "default",
                "do",
                "double",
                "else",
                "enum",
                "extends",
                "false",
                "final",
                "finally",
                "float",
                "for",
                "if",
                "implements",
                "import",
                "instanceof",
                "int",
                "interface",
                "long",
                "native",
                "new",
                "null",
                "package",
                "private",
                "protected",
                "public",
                "return",
                "short",
                "static",
                "strictfp",
                "super",
                "switch",
                "synchronized",
                "this",
                "throw",
                "throws",
                "transient",
                "true",
                "try",
                "void",
                "volatile",
                "while");

        for (String keyword : keywords) {
            if (expression.toLowerCase().contains(keyword)) {
                errors.add(new ParseError(expression.toLowerCase().indexOf(keyword), "Keyword is reserved: " + keyword));
            }
        }
        return errors;
    }
}
