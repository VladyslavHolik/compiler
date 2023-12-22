package com.holik.analyzer;

import com.holik.tokenizer.ParseError;

import java.util.List;
import java.util.Optional;

public interface Transition {
    Lexeme getFrom();

    Lexeme getTo();

    Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex);
}
