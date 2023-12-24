package com.holik.expression;

import java.util.List;

public interface Expression {
    String getNode();

    List<Expression> getChildren();

    Expression optimize();

    Expression optimizeMinuses();

    Expression paralelizePluses();

    List<Expression> getPlusOperands();

    Integer getLevel();

    Expression negateIfPossible();

    Expression negate();

    Expression divideIfPossible();

    Expression divide();
    Expression paralelizeMultiplication();

    List<Expression> getMultiplicationOperands();
    List<String> getFunctions();
}
