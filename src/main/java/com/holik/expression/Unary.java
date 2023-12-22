package com.holik.expression;

import com.holik.operation.Operation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Unary implements Expression {
    private final Expression expression;
    private final Operation operation;

    @Override
    public String getNode() {
        return operation.getOperator();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(expression);
    }

    @Override
    public Expression optimize() {
        var optimizedExpression = expression.optimize();
        if (optimizedExpression != null) {
            return new Unary(optimizedExpression, operation);
        }
        return null;
    }

    @Override
    public Expression optimizeMinuses() {
        if (operation.equals(Operation.SUBTRACTION)) {
            if (expression instanceof Binary expressionBinary) {
                if (expressionBinary.getOperation().equals(Operation.SUBTRACTION)) {
                    return new Binary(expressionBinary.getY(), expressionBinary.getX(), Operation.SUBTRACTION).optimizeMinuses();
                }
            } else if (expression instanceof Unary expressionUnary) {
                if (expressionUnary.operation.equals(Operation.SUBTRACTION)) {
                    return expressionUnary.expression.optimizeMinuses();
                }
            }
        }
        return expression.optimizeMinuses();
    }

    @Override
    public Expression paralelizePluses() {
        return new Unary(expression.paralelizePluses(), operation);
    }

    @Override
    public List<Expression> getPlusOperands() {
        return new ArrayList<>(List.of(this));
    }

    @Override
    public Integer getLevel() {
        return 1 + expression.getLevel();
    }
}
