package com.holik.expression;

import com.holik.operation.Operation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Call implements Expression {
    private final List<Expression> args;
    private final String function;


    @Override
    public String getNode() {
        return function;
    }

    @Override
    public List<Expression> getChildren() {
        return args;
    }

    @Override
    public Expression optimize() {
        var optimizedArgs = new ArrayList<Expression>();
        var wasOptimized = false;
        for (int i = 0; i < args.size(); i++) {
            var optimizedArg = args.get(i).optimize();
            if (optimizedArg != null) {
                wasOptimized = true;
                optimizedArgs.add(optimizedArg);
            } else {
                optimizedArgs.add(args.get(i));
            }
        }

        if (wasOptimized) {
            return new Call(optimizedArgs, function);
        }
        return null;
    }

    @Override
    public Expression optimizeMinuses() {
        var optimizedArgs = new ArrayList<Expression>();
        for (Expression arg : args) {
            var optimizedArg = arg.optimizeMinuses();
            optimizedArgs.add(optimizedArg);
        }
        return new Call(optimizedArgs, function);
    }

    @Override
    public Expression paralelizePluses() {
        var optimizedArgs = new ArrayList<Expression>();
        for (Expression arg : args) {
            var optimizedArg = arg.paralelizePluses();
            optimizedArgs.add(optimizedArg);
        }
        return new Call(optimizedArgs, function);
    }

    @Override
    public List<Expression> getPlusOperands() {
        var operands = new ArrayList<Expression>();
        operands.add(this);
        return operands;
    }

    @Override
    public Integer getLevel() {
        var maxLevel = 1;
        for (Expression arg : args) {
            if (arg.getLevel() > maxLevel) {
                maxLevel = arg.getLevel();
            }
        }
        return maxLevel + 1;
    }

    @Override
    public Expression negateIfPossible() {
        var optimizedArgs = new ArrayList<Expression>();
        for (Expression arg : args) {
            var optimizedArg = arg.negateIfPossible();
            optimizedArgs.add(optimizedArg);
        }
        return new Call(optimizedArgs, function);
    }

    @Override
    public Expression negate() {
        return new Unary(this.negateIfPossible(), Operation.SUBTRACTION);
    }

    @Override
    public Expression divideIfPossible() {
        var optimizedArgs = new ArrayList<Expression>();
        for (Expression arg : args) {
            var optimizedArg = arg.divideIfPossible();
            optimizedArgs.add(optimizedArg);
        }
        return new Call(optimizedArgs, function);
    }

    @Override
    public Expression divide() {
        return new Binary(new Constant("1"), this, Operation.DIVISION);
    }

    @Override
    public Expression paralelizeMultiplication() {
        var optimizedArgs = new ArrayList<Expression>();
        for (Expression arg : args) {
            var optimizedArg = arg.paralelizeMultiplication();
            optimizedArgs.add(optimizedArg);
        }
        return new Call(optimizedArgs, function);
    }

    @Override
    public List<Expression> getMultiplicationOperands() {
        var operands = new ArrayList<Expression>();
        operands.add(this);
        return operands;
    }
}
