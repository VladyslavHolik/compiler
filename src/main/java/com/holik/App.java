package com.holik;

import com.holik.analyzer.Analyzer;
import com.holik.expression.Expression;
import com.holik.matrix.MatrixComputerSystem;
import com.holik.optimizer.Optimizer;
import com.holik.parser.Parser;
import com.holik.tokenizer.Lexemes;
import com.holik.tokenizer.ParseError;
import com.holik.tokenizer.Tokenizer;
import de.vandermeer.asciitable.AsciiTable;
import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

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
            "a-b*k+b*t+1*2*3*4*5*6*7*0-b*f*f*5.9+b*f*q+b*g*f*5.9-b*g*q-b*w/p+b*y*m/p-b*y/p-x*x/(d+q-w)+3*x/(d+q-w)+3*x/(d+q-w)+3*3/(d+q-w)",
            "1+2+3+4+(5+6+7)+8+4*5*6*(7*8*9)*10*11+9/10/11/12/13/14/15/16 + 1*2*3*4*5*0",
            "1+2+3+(4+5+6+7+8)+9+10+11+12+13+14+15+16",
            "a-b-c-d-e-f-g-h-1-2-3-4-5-6-7-8",
            "a/b/c/d/e/f/g/h",
            "f(1,2) + f(1,3) + f(3,4) + f(5,6) + b(1,2) + b(1,3) + b(1,4)"
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

            List<Expression> expressions = new ArrayList<>();
            Set<String> functions = Set.copyOf(expression.getFunctions());
            Map<String, Integer> functionCosts = askFunctionCosts(functions);

            expressions.add(expression);

            var optimizedExpression = optimizer.optimize(expression);
            if (optimizedExpression != null) {
                System.out.println();
                System.out.println("Expression was optimized");
                System.out.println();
                expression = optimizedExpression;
                expressions.add(expression);
            }

            expression = expression.negateIfPossible();
            expression = expression.paralelizePluses();
            expression = expression.divideIfPossible();
            expression = expression.paralelizeMultiplication();
            optimizedExpression = optimizer.optimize(expression);
            if (optimizedExpression != null) {
                expression = optimizedExpression;
            }
            expressions.add(expression);

            List<List<Double>> executionStats = new ArrayList<>();

            var index = 0;
            for (Expression ex : expressions) {
                System.out.println("Index " + index++);
                printExpressionTree(ex);
                executionStats.add(getExecutionStats(ex, functionCosts));
            }

            printExecutionTable(executionStats);
            var indexOfBestTree = -1;
            Double minTacts = Double.MAX_VALUE;
            for (int i = 0; i < executionStats.size(); i++) {
                if (executionStats.get(i).get(0) < minTacts) {
                    minTacts = executionStats.get(i).get(0);
                    indexOfBestTree = i;
                }
            }
            System.out.println("Optimal tree in current matrix cs, index: " + indexOfBestTree);
        }
    }

    private void printExecutionTable(List<List<Double>> executionStats) {
        AsciiTable at = new AsciiTable();
        at.addRule();

        var header = new ArrayList<>(List.of("Index", "Tacts", "Acceleration coefficient", "Efficiency coefficient"));
        at.addRow(header);

        for (int i = 0; i < executionStats.size(); i++) {
            at.addRule();
            var stats = new ArrayList<String>();
            stats.add(String.valueOf(i));
            for (int j = 0; j < executionStats.get(i).size(); j++) {
                stats.add(String.valueOf(executionStats.get(i).get(j)));
            }
            at.addRow(stats);
        }
        at.addRule();
        at.getContext().setWidth(60);

        String rend = at.render();
        System.out.println(rend);
    }

    private List<Double> getExecutionStats(Expression expression, Map<String, Integer> functionCosts) {
        MatrixComputerSystem matrixComputerSystem = new MatrixComputerSystem();
        matrixComputerSystem.parseTree(expression, functionCosts);
        matrixComputerSystem.emulateExecution();
        matrixComputerSystem.printDiagram();
        return matrixComputerSystem.getExecutionStats();
    }

    private Map<String, Integer> askFunctionCosts(Set<String> functions) {
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
