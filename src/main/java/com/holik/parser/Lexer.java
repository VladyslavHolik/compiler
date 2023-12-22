package com.holik.parser;

import com.holik.analyzer.LexemeValue;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Lexer {
    private int cursor = -1;
    private final List<LexemeValue> lexemes;
    private LexemeValue token;

    public void next() {
        cursor += 1;
        if (cursor <= lexemes.size() - 1) {
            token = lexemes.get(cursor);
        }
    }

    public LexemeValue token() {
        return token;
    }
}
