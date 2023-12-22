package com.holik.tokenizer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ParseError {
    private int index;
    private String message;
}
