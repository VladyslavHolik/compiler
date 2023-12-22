package com.holik.analyzer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FunctionAnalyzer {
    public boolean isFunctionOpen(List<LexemeValue> lexemes) {
        List<String> stack = new ArrayList<>();
        for (int i = 0; i < lexemes.size(); i++) {
            if (lexemes.get(i).getLexeme() == Lexeme.OPENING_BRACKET) {
                if (i >= 1 && lexemes.get(i - 1).getLexeme() == Lexeme.FUNCTION_NAME) {
                    stack.add("Function");
                } else {
                    stack.add("Operation");
                }
            } else if (lexemes.get(i).getLexeme() == Lexeme.CLOSING_BRACKET) {
                stack.remove(stack.size() - 1);
            }
        }

        return stack.get(stack.size() - 1).equals("Function");
    }
}
