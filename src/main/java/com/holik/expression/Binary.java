package com.holik.expression;

import com.google.common.collect.Lists;
import com.holik.operation.Operation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
public class Binary implements Expression {
    private final Expression x;
    private final Expression y;
    private final Operation operation;


    @Override
    public String getNode() {
        return operation.getOperator();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(x, y);
    }

    @Override
    public Expression optimize() {
        if (operation.equals(Operation.MULTIPLICATION)) {
            if (x.getNode().equals("1") || x.getNode().equals("1.0")) {
                return y;
            } else if (y.getNode().equals("1") || y.getNode().equals("1.0")) {
                return x;
            } else if (x.getNode().equals("0") || y.getNode().equals("0") || x.getNode().equals("0.0") || y.getNode().equals("0.0")) {
                return new Constant("0");
            }
        } else if (operation.equals(Operation.ADDITION)) {
            if (x.getNode().equals("0") || x.getNode().equals("0.0")) {
                return y;
            } else if (y.getNode().equals("0") || y.getNode().equals("0.0")) {
                return x;
            }
        } else if (operation.equals(Operation.DIVISION)) {
            if (x.getNode().equals("1") && y.getNode().equals("1")) {
                return x;
            }
        }

        var xOptimized = x.optimize();
        var yOptimized = y.optimize();

        if (xOptimized != null || yOptimized != null) {
            return new Binary(xOptimized == null ? x : xOptimized, yOptimized == null ? y : yOptimized, operation);
        }
        return null;
    }

    @Override
    public Expression optimizeMinuses() {
        if (operation.equals(Operation.SUBTRACTION)) {
            if (x instanceof Binary xBinary) {
                if (xBinary.operation.equals(Operation.SUBTRACTION)) {
                    return new Binary(xBinary.x, new Binary(xBinary.y, y, Operation.ADDITION), Operation.SUBTRACTION).optimizeMinuses();
                }
            } else if (y instanceof Binary yBinary) {
                if (yBinary.operation.equals(Operation.SUBTRACTION)) {
                    return new Binary(new Binary(x, yBinary.x, Operation.SUBTRACTION), yBinary.y, Operation.ADDITION).optimizeMinuses();
                }
            }
        }
        return new Binary(x.optimizeMinuses(), y.optimizeMinuses(), operation);
    }

    @Override
    public Expression paralelizePluses() {
        if (operation.equals(Operation.ADDITION)) {
            var operands = getPlusOperands();
            if (operands.size() > 2) {
                operands.sort(Comparator.comparingInt(Expression::getLevel));
                while (operands.size() != 2) {
                    var newOperandsList = new ArrayList<Expression>();
                    var partitions = Lists.partition(operands, 2);
                    for (List<Expression> partition : partitions) {
                        if (partition.size() == 2) {
                            newOperandsList.add(new Binary(partition.get(0), partition.get(1), Operation.ADDITION));
                        } else {
                            newOperandsList.add(partition.get(0));
                        }
                    }
                    operands = newOperandsList;
                }
            }
            return new Binary(operands.get(0).paralelizePluses(), operands.get(1).paralelizePluses(), Operation.ADDITION);

        }
        return new Binary(x.paralelizePluses(), y.paralelizePluses(), operation);
    }

    @Override
    public List<Expression> getPlusOperands() {
        var operands = new ArrayList<Expression>();
        if (operation.equals(Operation.ADDITION)) {
            operands.addAll(x.getPlusOperands());
            operands.addAll(y.getPlusOperands());
            return operands;
        }
        operands.add(this);
        return operands;
    }

    @Override
    public Integer getLevel() {
        return Math.max(x.getLevel() + 1, y.getLevel() + 1);
    }

    @Override
    public Expression negateIfPossible() {
        if (operation.equals(Operation.SUBTRACTION)) {
            return new Binary(x.negateIfPossible(), y.negate(), Operation.ADDITION);
        }
        return new Binary(x.negateIfPossible(), y.negateIfPossible(), operation);
    }

    @Override
    public Expression negate() {
        if (operation.equals(Operation.ADDITION)) {
            return new Binary(x.negate(), y.negate(), Operation.ADDITION);
        } else if (operation.equals(Operation.SUBTRACTION)) {
            return new Binary(y.negateIfPossible(), x.negate(), Operation.ADDITION);
        }
        return new Binary(x.negate(), y.negateIfPossible(), operation);
    }

    @Override
    public Expression divideIfPossible() {
        if (operation.equals(Operation.DIVISION)) {
            return new Binary(x.divideIfPossible(), y.divide(), Operation.MULTIPLICATION);
        }
        return new Binary(x.divideIfPossible(), y.divideIfPossible(), operation);
    }

    @Override
    public Expression divide() {
        if (operation.equals(Operation.MULTIPLICATION)) {
            return new Binary(new Constant("1"), this.divideIfPossible(), Operation.DIVISION);
        } else if (operation.equals(Operation.DIVISION)) {
            return new Binary(y.divideIfPossible(), x.divide(), Operation.MULTIPLICATION);
        }
        return new Binary(new Constant("1"), this.divideIfPossible(), Operation.DIVISION);
    }

    @Override
    public Expression paralelizeMultiplication() {
        if (operation.equals(Operation.MULTIPLICATION)) {
            var operands = getMultiplicationOperands();
            if (operands.size() > 2) {
                operands.sort(Comparator.comparingInt(Expression::getLevel));
                while (operands.size() != 2) {
                    var newOperandsList = new ArrayList<Expression>();
                    var partitions = Lists.partition(operands, 2);
                    for (List<Expression> partition : partitions) {
                        if (partition.size() == 2) {
                            newOperandsList.add(new Binary(partition.get(0), partition.get(1), Operation.MULTIPLICATION));
                        } else {
                            newOperandsList.add(partition.get(0));
                        }
                    }
                    operands = newOperandsList;
                }
            }
            return new Binary(operands.get(0).paralelizeMultiplication(), operands.get(1).paralelizeMultiplication(), Operation.MULTIPLICATION);

        }
        return new Binary(x.paralelizeMultiplication(), y.paralelizeMultiplication(), operation);
    }

    @Override
    public List<Expression> getMultiplicationOperands() {
        var operands = new ArrayList<Expression>();
        if (operation.equals(Operation.MULTIPLICATION)) {
            operands.addAll(x.getMultiplicationOperands());
            operands.addAll(y.getMultiplicationOperands());
            return operands;
        }
        operands.add(this);
        return operands;
    }

    @Override
    public List<String> getFunctions() {
        var functions = new ArrayList<String>();
        functions.addAll(x.getFunctions());
        functions.addAll(y.getFunctions());
        return functions;
    }

    @Override
    public String toString() {
        return operation.getOperator() + "[" + x.toString() + "," + y.toString() + "]";
    }
}
