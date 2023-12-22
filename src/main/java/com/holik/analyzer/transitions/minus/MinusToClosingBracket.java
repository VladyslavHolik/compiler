package com.holik.analyzer.transitions.minus;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.*;
@Component
public class MinusToClosingBracket implements Transition {

    @Override
    public Lexeme getFrom() {
        return MINUS;
    }

    @Override
    public Lexeme getTo() {
        return CLOSING_BRACKET;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
        return Optional.of(new ParseError(index, "No value is present after minus operator"));
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
