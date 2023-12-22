package com.holik.analyzer.transitions.closing.bracket;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.*;

@Component
public class ClosingBracketToEnd implements Transition {

    @Override
    public Lexeme getFrom() {
        return CLOSING_BRACKET;
    }

    @Override
    public Lexeme getTo() {
        return END;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var quantityOfOpenBrackets = lexemes.subList(0, currentLexemeIndex + 1).stream().map(LexemeValue::getLexeme).filter(OPENING_BRACKET::equals).count();
        var quantityOfCloseBrackets = lexemes.subList(0, currentLexemeIndex + 1).stream().map(LexemeValue::getLexeme).filter(CLOSING_BRACKET::equals).count();

        if (quantityOfCloseBrackets != quantityOfOpenBrackets) {
            return Optional.of(new ParseError(lexemes.get(currentLexemeIndex).getIndex(), "Not all brackets are closed"));
        } else {
            return Optional.empty();
        }
    }
}
