package com.holik.analyzer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LexemeValue {
    private Lexeme lexeme;
    private String value;
    private int lexemeIndex;
    private int index;
}
