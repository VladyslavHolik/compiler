package com.holik.tokenizer;

import com.holik.analyzer.LexemeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Lexemes {
    private final List<LexemeValue> values;
    private final List<ParseError> errors;
}
