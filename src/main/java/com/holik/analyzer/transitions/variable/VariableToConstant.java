package com.holik.analyzer.transitions.variable;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.CONSTANT;
import static com.holik.analyzer.Lexeme.VARIABLE;
@Component
public class VariableToConstant implements Transition {

    @Override
    public Lexeme getFrom() {
        return VARIABLE;
    }

    @Override
    public Lexeme getTo() {
        return CONSTANT;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
        return Optional.of(new ParseError(index, "No operator is present between variable and constant"));
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
