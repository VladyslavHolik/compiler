package com.holik.analyzer.transitions.opening.bracket;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.MULTIPLY;
import static com.holik.analyzer.Lexeme.OPENING_BRACKET;
@Component
public class OpeningBracketToMultiply implements Transition {

    @Override
    public Lexeme getFrom() {
        return OPENING_BRACKET;
    }

    @Override
    public Lexeme getTo() {
        return MULTIPLY;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
        return Optional.of(new ParseError(index, "Multiplication is not allowed after opening bracket"));
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
