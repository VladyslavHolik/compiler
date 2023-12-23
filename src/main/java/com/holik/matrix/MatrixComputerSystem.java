package com.holik.matrix;

import com.holik.expression.Constant;
import com.holik.expression.Expression;
import com.holik.expression.Variable;
import de.vandermeer.asciitable.AsciiTable;
import org.checkerframework.checker.units.qual.A;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.groupingBy;

@Component
public class MatrixComputerSystem {
    private Graph<Processor, DefaultEdge> g;
    private Map<Integer, List<Expression>> expressionsPerLevel = new HashMap<>();

    public MatrixComputerSystem() {
        g = new DefaultUndirectedGraph<>(DefaultEdge.class);

        Processor processor1 = new Processor("p1");
        Processor processor2 = new Processor("p2");
        Processor processor3 = new Processor("p3");
        Processor processor4 = new Processor("p4");
        Processor processor5 = new Processor("p5");
        Processor processor6 = new Processor("p6");
        Processor processor7 = new Processor("p7");
        Processor processor8 = new Processor("p8");
        Processor processor9 = new Processor("p9");

        // add the vertices
        g.addVertex(processor1);
        g.addVertex(processor2);
        g.addVertex(processor3);
        g.addVertex(processor4);
        g.addVertex(processor5);
        g.addVertex(processor6);
        g.addVertex(processor7);
        g.addVertex(processor8);
        g.addVertex(processor9);

        // add edges to create linking structure
        g.addEdge(processor1, processor2);
        g.addEdge(processor2, processor3);
        g.addEdge(processor3, processor6);
        g.addEdge(processor6, processor9);
        g.addEdge(processor9, processor8);
        g.addEdge(processor8, processor7);
        g.addEdge(processor7, processor4);
        g.addEdge(processor4, processor1);
        g.addEdge(processor2, processor5);
        g.addEdge(processor5, processor6);
        g.addEdge(processor5, processor8);
        g.addEdge(processor5, processor4);
    }

    public Integer shortestPathLength(Processor processorA, Processor processorB) {
        DijkstraShortestPath<Processor, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(g);

        ShortestPathAlgorithm.SingleSourcePaths<Processor, DefaultEdge> cPaths = dijkstraAlg.getPaths(processorA);
        return cPaths.getPath(processorB).getLength();
    }

    public void parseTree(Expression expression) {
        addExpressionsPerLevel(expression);
    }

    private void addExpressionsPerLevel(Expression expression) {
        var expressionLevel = expression.getLevel();
        List<Expression> expressionsOnCurrentLevel = expressionsPerLevel.get(expressionLevel) == null ? new ArrayList<>() : expressionsPerLevel.get(expressionLevel);
        if (!(expression instanceof Constant || expression instanceof Variable)) {
            expressionsOnCurrentLevel.add(expression);
            expressionsPerLevel.put(expressionLevel, expressionsOnCurrentLevel);

            expression.getChildren().forEach(this::addExpressionsPerLevel);
        } else {
            g.vertexSet().forEach(processor -> processor.getMemory().put(expression, true));
        }
    }

    public void emulateExecution() {
        var expressionPerLevelSet = new ArrayList<>(expressionsPerLevel.entrySet().stream().toList());
        expressionPerLevelSet.sort(Comparator.comparingInt(Map.Entry::getKey));
        expressionPerLevelSet.forEach(expressionOnLevelEntry -> {
                    var expressionOnLevel = expressionOnLevelEntry.getValue();
                    var expressionOnLevelPerType = expressionOnLevel.stream().collect(groupingBy(Expression::getNode)).entrySet().stream().toList();
                    for (int i = 0; i < expressionOnLevelPerType.size(); i++) {
                        var expressionsOnLevelPerTypeList = expressionOnLevelPerType.get(i);
                        while (!areAllOperationsCompleted(expressionsOnLevelPerTypeList.getValue())) {
                            for (int j = 0; j < expressionsOnLevelPerTypeList.getValue().size(); j++) {
                                if (!isOperationCompleted(expressionsOnLevelPerTypeList.getValue().get(j))) {
                                    executeOperation(expressionsOnLevelPerTypeList.getValue().get(j));
                                }
                            }
                            waitUntilProcessorsAvailable();
                        }
                    }
                }
        );
    }

    private Boolean areAllOperationsCompleted(List<Expression> expressions) {
        for (Expression expression : expressions) {
            if (!isOperationCompleted(expression)) {
                return false;
            }
        }
        return true;
    }

    private Boolean isOperationCompleted(Expression expression) {
        return g.vertexSet().stream().anyMatch(processor -> processor.getMemory().containsKey(expression));
    }

    public void executeOperation(Expression expression) {
        var processorToExecuteOpt = g.vertexSet().stream().filter(processor -> {
            for (Expression child : expression.getChildren()) {
                if (!processor.getMemory().containsKey(child)) {
                    return false;
                }
            }
            return processor.isAvailable();
        }).findFirst();
        if (processorToExecuteOpt.isPresent()) {
            processorToExecuteOpt.get().assignOperation(expression, getOperationCost(expression));
        } else {
            var processorThatAlreadyHasAllDataButIsUnavailable = g.vertexSet().stream().filter(processor -> {
                for (Expression child : expression.getChildren()) {
                    if (!processor.getMemory().containsKey(child)) {
                        return false;
                    }
                }
                return true;
            }).findFirst();
            if (processorThatAlreadyHasAllDataButIsUnavailable.isPresent()) {
                return;
            }
            processorToExecuteOpt = g.vertexSet().stream().filter(processor -> {
                var containsAtLeastOneChild = false;
                for (Expression child : expression.getChildren()) {
                    if (processor.getMemory().containsKey(child)) {
                        containsAtLeastOneChild = true;
                        break;
                    }
                }
                return processor.isAvailable() && containsAtLeastOneChild;
            }).findFirst();
            if (processorToExecuteOpt.isEmpty()) {
                return;
            }
            var processorToExecute = processorToExecuteOpt.get();
            var unfoundChildOpt = expression.getChildren().stream().filter(child -> !processorToExecute.getMemory().containsKey(child)).findFirst();
            if (unfoundChildOpt.isEmpty()) {
                processorToExecute.assignOperation(expression, getOperationCost(expression));
                return;
            } else {
                var unfoundChild = unfoundChildOpt.get();
                var processorWithChild = g.vertexSet().stream().filter(processor -> processor.getMemory().containsKey(unfoundChild) && processor.isAvailable()).findFirst();
                while (processorWithChild.isEmpty()) {
                    runTact();
                    processorWithChild = g.vertexSet().stream().filter(processor -> processor.getMemory().containsKey(unfoundChild) && processor.isAvailable()).findFirst();
                }
                var tacts = shortestPathLength(processorToExecute, processorWithChild.get());
                processorToExecute.assignReadOperation(unfoundChild, processorWithChild.get(), tacts);
                processorWithChild.get().assignWriteOperation(processorToExecute, unfoundChild, tacts);
            }
        }
    }

    public void runTact() {
        g.vertexSet().forEach(Processor::runTact);
    }

    public void waitUntilProcessorsAvailable() {
        while (g.vertexSet().stream().anyMatch(processor -> !processor.isAvailable())) {
            runTact();
        }
    }

    public void waitUntilAtLeastOneProcessorAvailable() {
        while (g.vertexSet().stream().noneMatch(Processor::isAvailable)) {
            runTact();
        }
    }

    public Integer getOperationCost(Expression expression) {
        if (expression.getNode().equals("+")) {
            return 1;
        } else if (expression.getNode().equals("-")) {
            return 2;
        } else if (expression.getNode().equals("*")) {
            return 3;
        } else if (expression.getNode().equals("/")) {
            return 4;
        }
        throw new RuntimeException("Cannot get cost for expression: " + expression);
    }

    public void printDiagram() {
        AsciiTable at = new AsciiTable();
        var processors = new ArrayList<>(g.vertexSet().stream().toList());
        //processors.sort(Comparator.co(Expression::getLevel));

        at.addRule();
        var header = new ArrayList<String>();
        processors.forEach(processor -> header.add(processor.getName()));
        at.addRow(header);

        for (int i = 0; i < processors.get(0).getOperationPerTact().size(); i++) {
            at.addRule();
            var tactOperations = new ArrayList<String>();
            for (int j = 0; j < processors.size(); j++) {
                var processor = processors.get(j);
                tactOperations.add(processor.getOperationPerTact().get(i));
            }
            at.addRow(tactOperations);
        }
        at.addRule();
        at.getContext().setWidth(150);

        String rend = at.render();
        System.out.println(rend);
        System.out.println("Quantity of tacts: " + processors.get(0).getOperationPerTact().size());
    }
}
