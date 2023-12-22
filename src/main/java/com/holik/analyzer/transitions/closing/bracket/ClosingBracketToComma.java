package com.holik.analyzer.transitions.closing.bracket;

import com.holik.analyzer.FunctionAnalyzer;
import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.*;

@Component
public class ClosingBracketToComma implements Transition {

    @Autowired
    private FunctionAnalyzer functionAnalyzer;

    @Override
    public Lexeme getFrom() {
        return CLOSING_BRACKET;
    }

    @Override
    public Lexeme getTo() {
        return COMMA;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        if (!functionAnalyzer.isFunctionOpen(lexemes.subList(0, currentLexemeIndex + 1))) {
            var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
            return Optional.of(new ParseError(index, "Comma is not allowed, because expression is not function argument"));
        } else {
            return Optional.empty();
        }
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
