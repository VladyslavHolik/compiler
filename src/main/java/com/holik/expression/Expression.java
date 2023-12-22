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
}
