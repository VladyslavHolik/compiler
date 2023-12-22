package com.holik.expression;

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
}
