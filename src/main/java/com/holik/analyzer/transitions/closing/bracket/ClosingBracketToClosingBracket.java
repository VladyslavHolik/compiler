package com.holik.analyzer.transitions.closing.bracket;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.analyzer.Transition;
import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.holik.analyzer.Lexeme.CLOSING_BRACKET;
import static com.holik.analyzer.Lexeme.OPENING_BRACKET;

@Component
public class ClosingBracketToClosingBracket implements Transition {

    @Override
    public Lexeme getFrom() {
        return CLOSING_BRACKET;
    }

    @Override
    public Lexeme getTo() {
        return CLOSING_BRACKET;
    }

    @Override
    public Optional<ParseError> getError(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        var quantityOfOpenBrackets = lexemes.subList(0, currentLexemeIndex + 1).stream().map(LexemeValue::getLexeme).filter(OPENING_BRACKET::equals).count();
        var quantityOfCloseBrackets = lexemes.subList(0, currentLexemeIndex + 1).stream().map(LexemeValue::getLexeme).filter(CLOSING_BRACKET::equals).count();

        if (quantityOfCloseBrackets + 1 > quantityOfOpenBrackets) {
            var index = nextLexeme(lexemes, currentLexemeIndex).getIndex();
            return Optional.of(new ParseError(index, "Bracket is closed without opening"));
        } else {
            return Optional.empty();
        }
    }

    private LexemeValue nextLexeme(List<LexemeValue> lexemes, Integer currentLexemeIndex) {
        return lexemes.get(currentLexemeIndex + 1);
    }
}
