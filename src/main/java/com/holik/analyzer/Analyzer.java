package com.holik.analyzer;

import com.holik.tokenizer.ParseError;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Analyzer {

    private Map<Lexeme, Map<Lexeme, Transition>> transitions;

    public Analyzer(List<Transition> transitionList) {
        transitions = new HashMap<>();

        for (Transition transition : transitionList) {
            var transitionsWithStartingLexeme = transitions.get(transition.getFrom());
            if (transitionsWithStartingLexeme == null) {
                var transitionsWithEndLexeme = new HashMap<Lexeme, Transition>();
                transitionsWithEndLexeme.put(transition.getTo(), transition);

                transitions.put(transition.getFrom(), transitionsWithEndLexeme);
            } else {
                transitionsWithStartingLexeme.put(transition.getTo(), transition);
            }
        }
    }

    public List<ParseError> analyze(List<LexemeValue> lexemeValues) {
        List<ParseError> errors = new ArrayList<>();

        for (int i = 0; i < lexemeValues.size() - 1; i++) {
            var currentLexeme = lexemeValues.get(i);
            var nextLexeme = lexemeValues.get(i + 1);

            if (transitions.get(currentLexeme.getLexeme()) != null && transitions.get(currentLexeme.getLexeme()).get(nextLexeme.getLexeme()) != null) {
                var transition = transitions.get(currentLexeme.getLexeme()).get(nextLexeme.getLexeme());
                var optionalError = transition.getError(lexemeValues, i);
                optionalError.ifPresent(errors::add);
            }
        }

        return errors;
    }
}
