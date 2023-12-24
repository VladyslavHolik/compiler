package com.holik;

import com.holik.analyzer.Analyzer;
import com.holik.expression.Expression;
import com.holik.matrix.MatrixComputerSystem;
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
import java.util.Map;
import java.util.Scanner;

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

    @Autowired
    private MatrixComputerSystem matrixComputerSystem;

    private static final List<String> expressions = List.of(
            "a(1,2,3) + 1+2+3 + b(3,5) + f(1)",
            "a+b+c+d+e+f+g+h+k+l+m+n",
            "1+2+3+4+5+6+7+8+4*5*6*7*8*9*10*11+9/10/11/12/13/14/15/16",
            "1+2+3+4+5+6+7+8+9+10+11+12+13+14+15+16",
            "1+2+3-4+5+6-7+(8+9+10)+(12+13)*2*3*5*5",
            "i/1.0 + 0 - 0*k*h + 2 - 4.8/2 + 1*e/2",
            "a-b-c-d-e-f-g-h",
            "a/b/c/d/e/f/g/h",
            "-(-(a))",
            "a+(b+c+d+(e+f)+g)+h",
            "a-((b-c-d)-(e-f)-g)-h-(1*2*3*4)",
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
        String expressionString = expressions.get(0);
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

            expression = expression.negateIfPossible();
            expression = expression.paralelizePluses();
            expression = expression.divideIfPossible();
            expression = expression.paralelizeMultiplication();
            optimizedExpression = optimizer.optimize(expression);
            if (optimizedExpression != null) {
                expression = optimizedExpression;
            }
            printExpressionTree(expression);
            List<String> functions = expression.getFunctions();
            Map<String, Integer> functionCosts = askFunctionCosts(functions);

            matrixComputerSystem.parseTree(expression, functionCosts);
            matrixComputerSystem.emulateExecution();
            matrixComputerSystem.printDiagram();
        }
    }

    private Map<String, Integer> askFunctionCosts(List<String> functions) {
        Map<String, Integer> functionCosts = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        for (String function : functions) {
            System.out.println("Enter function '" + function + "' cost: ");
            String cost = scanner.nextLine();
            functionCosts.put(function, Integer.parseInt(cost));
        }
        return functionCosts;
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
