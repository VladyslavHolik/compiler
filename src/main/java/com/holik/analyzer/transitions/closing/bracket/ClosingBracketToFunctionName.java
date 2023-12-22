package com.holik.analyzer.transitions.closing.bracket;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.CLOSING_BRACKET;
import static com.holik.analyzer.Lexeme.FUNCTION_NAME;

@Component
public class ClosingBracketToFunctionName implements Transition {

    @Override
    public Lexeme getFrom() {
        return CLOSING_BRACKET;
    }

    @Override
    public Lexeme getTo() {
        return FUNCTION_NAME;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
        return Optional.of(new ParseError(index, "Function is not allowed after bracket without any operation"));
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
