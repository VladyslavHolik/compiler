package com.holik.operation;

public enum Operation {
    ADDITION("+"),
    SUBTRACTION("-"),
    MULTIPLICATION("*"),
    DIVISION("/");

    private final String operator;

    Operation(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
