package com.holik.expression;

import com.holik.operation.Operation;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Constant implements Expression {
    private final String value;

    @Override
    public String getNode() {
        return value;
    }

    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Expression optimize() {
        return null;
    }

    @Override
    public Expression optimizeMinuses() {
        return this;
    }

    @Override
    public Expression paralelizePluses() {
        return this;
    }

    @Override
    public List<Expression> getPlusOperands() {
        var operands = new ArrayList<Expression>();
        operands.add(this);
        return operands;
    }

    @Override
    public Integer getLevel() {
        return 1;
    }

    @Override
    public Expression negateIfPossible() {
        return this;
    }

    @Override
    public Expression negate() {
        return new Constant("-" + value);
    }

    @Override
    public Expression divideIfPossible() {
        return this;
    }

    @Override
    public Expression divide() {
        return new Binary(new Constant("1"), this, Operation.DIVISION);
    }

    @Override
    public Expression paralelizeMultiplication() {
        return this;
    }

    @Override
    public List<Expression> getMultiplicationOperands() {
        var operands = new ArrayList<Expression>();
        operands.add(this);
        return operands;
    }
}
