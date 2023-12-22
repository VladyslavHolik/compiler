package com.holik.optimizer;

import com.holik.expression.Expression;
import org.springframework.stereotype.Component;

@Component
public class Optimizer {
    public Expression optimize(Expression expression) {
        var optimizedExpression = expression.optimize();
        if (optimizedExpression != null) {
            var nextOptimized = optimize(optimizedExpression);
            return nextOptimized == null ? optimizedExpression : nextOptimized;
        }
        return null;
    }
}
