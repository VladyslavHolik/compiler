package com.holik;

import com.holik.analyzer.Analyzer;
import com.holik.expression.Expression;
import com.holik.optimizer.Optimizer;
import com.holik.parser.Parser;
import com.holik.tokenizer.Lexemes;
import com.holik.tokenizer.ParseError;
import com.holik.tokenizer.Tokenizer;
import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.List;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Autowired
    private Tokenizer tokenizer;
    @Autowired
    private Analyzer analyzer;

    @Autowired
    private Parser parser;

    @Autowired
    private Optimizer optimizer;

    private static final List<String> expressions = List.of(
            "a+b+c+d+e+f+g+h",
            "a-b-c-d-e-f-g-h",
            "-(-(a))",
            "a+(b+c+d+(e+f)+g)+h",
            "a-((b-c-d)-(e-f)-g)-h",
            "-(-exp(3*et/4.0+.2, 21-1)/L+23)*((a*78)*f(78)) + ((i+1+1) + (1+1+i/(i-1-1))/k/1/1) +6.000+.500+.5",
            "A-B*c-J*(d*t*j-u*t+c*r-1+w-k/q+m*(n-k*s+z*(y+u*p-y/r-5)+x+t/2))/r+P",
            "a*b+a*c+b*c",
            "5 / 9 * (((F - 32)))",
            "123 - 345 + 354 + 234 / 5 * 8",
            "fun(((12 + 324))) - ab + 234 * (234 + 12 - funA(funB(12 + 12), 12, 89, bk))",
            "1 + 2 * 0 * 5 - 3 * 1"
    );

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String expressionString = expressions.get(4);
        Lexemes lexemes = tokenizer.tokenize(expressionString);
        List<ParseError> errors = lexemes.getErrors();
        errors.addAll(analyzer.analyze(lexemes.getValues()));

        System.out.println();
        System.out.println(expressionString);
        printErrors(errors, expressionString);

        for (ParseError error : errors) {
            System.out.println("Error: " + error.getMessage() + " at index: " + error.getIndex());
        }

        if (errors.isEmpty()) {
            System.out.println("Expression is valid");

            var lexemeCount = lexemes.getValues().size();
            Expression expression = parser.parse(lexemes.getValues().subList(1, lexemeCount - 1));
            printExpressionTree(expression);

            var optimizedExpression = optimizer.optimize(expression);
            if (optimizedExpression != null) {
                System.out.println();
                System.out.println("Expression was optimized");
                System.out.println();
                expression = optimizedExpression;
                printExpressionTree(expression);
            }

            expression = expression.optimizeMinuses();
            printExpressionTree(expression);
            expression = expression.paralelizePluses();
            printExpressionTree(expression);
        }
    }

    public void printErrors(List<ParseError> errors, String expression) {
        var errorIndexMap = new HashMap<Integer, Boolean>();
        errors.forEach(e -> errorIndexMap.put(e.getIndex(), true));

        for (int i = 0; i < expression.length(); i++) {
            if (errorIndexMap.containsKey(i)) {
                System.out.print("^");
            } else {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    public static void printExpressionTree(Expression expression) {
        SimpleTreeNode prRoot = new SimpleTreeNode(expression.getNode());
        getExpressionTree(prRoot, expression);
        new TraditionalTreePrinter().print(prRoot);
    }

    private static void getExpressionTree(SimpleTreeNode prNode, Expression expression) {
        for (Expression child : expression.getChildren()) {
            SimpleTreeNode prChild = new SimpleTreeNode(child.getNode());
            prNode.addChild(prChild);
            getExpressionTree(prChild, child);
        }
    }
}
