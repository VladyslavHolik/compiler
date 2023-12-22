package com.holik.analyzer.transitions.divide;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.DIVIDE;
import static com.holik.analyzer.Lexeme.MULTIPLY;

@Component
public class DivideToDivide implements Transition {

    @Override
    public Lexeme getFrom() {
        return DIVIDE;
    }

    @Override
    public Lexeme getTo() {
        return DIVIDE;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
        return Optional.of(new ParseError(index, "Division is not allowed after another divide operator"));
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
