package com.holik.analyzer.transitions.divide;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.*;

@Component
public class DivideToEnd implements Transition {

    @Override
    public Lexeme getFrom() {
        return DIVIDE;
    }

    @Override
    public Lexeme getTo() {
        return END;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return Optional.of(new ParseError(lexemes.get(currentLexemeIndex).getIndex(), "Division without operand"));
    }
}
