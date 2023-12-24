package com.holik.matrix;

import com.holik.App;
import com.holik.expression.Expression;
import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Processor {
    private String name;
    private Map<Expression, Boolean> memory;
    private List<String> operationPerTact;
    private Integer quantityOfTactsLeft = 0;
    private Expression evaluatedExpression;
    private Expression expressionToRead;
    private String RWOperation;

    public Processor(String name) {
        this.name = name;
        memory = new HashMap<>();
        operationPerTact = new ArrayList<>();
    }

    public Boolean isAvailable() {
        return quantityOfTactsLeft == 0;
    }

    public void assignOperation(Expression expression, Integer quantityOfTacts) {
        quantityOfTactsLeft += quantityOfTacts;
        evaluatedExpression = expression;
    }

    public void assignReadOperation(Expression expressionToRead, Processor processorToReadFrom, Integer quantityOfTacts) {
        quantityOfTactsLeft += quantityOfTacts;
        this.expressionToRead = expressionToRead;
        RWOperation = "R(" + processorToReadFrom.getName() + "):" + expressionToRead.toString();
    }

    public void assignWriteOperation(Processor processorToWriteTo, Expression expressionToWrite, Integer quantityOfTacts) {
        quantityOfTactsLeft += quantityOfTacts;
        RWOperation = "W(" + processorToWriteTo.getName() + "):" + expressionToWrite.toString();;
    }

    public void runTact() {
        quantityOfTactsLeft = Math.max(0, quantityOfTactsLeft - 1);
        if (evaluatedExpression != null) {
            operationPerTact.add(evaluatedExpression.toString());
        } else if (RWOperation != null) {
            operationPerTact.add(RWOperation);
        } else {
            operationPerTact.add("");
        }
        if (isAvailable() && evaluatedExpression != null) {
            memory.put(evaluatedExpression, true);
            evaluatedExpression = null;
        } else if (isAvailable() && RWOperation != null) {
            if (expressionToRead != null) {
                memory.put(expressionToRead, true);
            }
            expressionToRead = null;
            RWOperation = null;
        }
    }
}
