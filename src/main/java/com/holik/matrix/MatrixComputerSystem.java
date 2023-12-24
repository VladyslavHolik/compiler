package com.holik.matrix;

import com.google.common.collect.Lists;
import com.holik.expression.Constant;
import com.holik.expression.Expression;
import com.holik.expression.Variable;
import de.vandermeer.asciitable.AsciiTable;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.groupingBy;

public class MatrixComputerSystem {
    private Graph<Processor, DefaultEdge> g;
    private Map<Integer, List<Expression>> expressionsPerLevel = new HashMap<>();
    private Map<String, Integer> functionCosts;

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

    public void parseTree(Expression expression, Map<String, Integer> functionCosts) {
        addExpressionsPerLevel(expression);
        this.functionCosts = functionCosts;
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
                        var expressionsOnLevelPerTypeList = expressionOnLevelPerType.get(i).getValue();
                        var partitions = Lists.partition(expressionsOnLevelPerTypeList, getProcessors().size());
                        for (List<Expression> partition : partitions) {
                            run(partition);
                        }
                    }
                }
        );
    }

    private void run(List<Expression> expressions) {
        expressions.sort(Comparator.comparingInt(e -> -e.getChildren().size()));
        Map<Processor, Expression> processorExpressionMap = new HashMap<>();
        Map<Expression, Processor> expressionProcessorMap = new HashMap<>();
        for (Expression expression : expressions) {
            var processorToExecuteExpression = getProcessors().stream().sorted(Comparator.comparingInt(p -> {
                var quantityOfChildrenPresent = 0;
                for (Expression child : expression.getChildren()) {
                    if (p.getMemory().containsKey(child)) {
                        quantityOfChildrenPresent++;
                    }
                }
                return -quantityOfChildrenPresent;
            })).filter(p -> !processorExpressionMap.containsKey(p)).findFirst().get();

            processorExpressionMap.put(processorToExecuteExpression, expression);
            expressionProcessorMap.put(expression, processorToExecuteExpression);
        }

        // transferring children
        while (!areAllExpressionsReadyToExecute(expressions, expressionProcessorMap)) {
            for (Expression expression : expressions) {
                var processor = expressionProcessorMap.get(expression);
                var childThatAreNotPresentOnMainProcessor = new ArrayList<Expression>();
                for (Expression child : expression.getChildren()) {
                    if (!processor.getMemory().containsKey(child)) {
                        childThatAreNotPresentOnMainProcessor.add(child);
                    }
                }

                for (Expression childToTransfer : childThatAreNotPresentOnMainProcessor) {
                    var processorWithChild = getProcessors().stream().filter(Processor::isAvailable).filter(p -> p.getMemory().containsKey(childToTransfer)).findFirst();
                    processorWithChild.ifPresent(pWithChild -> {
                        if (processor.isAvailable()) {
                            var tacts = shortestPathLength(processor, pWithChild);
                            processor.assignReadOperation(childToTransfer, pWithChild, tacts);
                            pWithChild.assignWriteOperation(processor, childToTransfer, tacts);
                        }
                    });
                }
            }
            if (!areAllExpressionsReadyToExecute(expressions, expressionProcessorMap)) {
                runTact();
            }
        }

        // starting execution
        for (Expression expression : expressions) {
            var processor = expressionProcessorMap.get(expression);
            processor.assignOperation(expression, getOperationCost(expression));
        }

        waitUntilProcessorsAvailable();
    }

    private Boolean areAllExpressionsReadyToExecute(List<Expression> expressions, Map<Expression, Processor> expressionProcessorMap) {
        var areAllExpressionsReadeToExecute = true;
        for (Expression expression : expressions) {
            var processor = expressionProcessorMap.get(expression);
            for (Expression child : expression.getChildren()) {
                if (!processor.getMemory().containsKey(child)) {
                    areAllExpressionsReadeToExecute = false;
                    break;
                }
            }
        }
        return areAllExpressionsReadeToExecute;
    }

    private List<Processor> getProcessors() {
        return new ArrayList<>(g.vertexSet().stream().toList());
    }

    public void runTact() {
        g.vertexSet().forEach(Processor::runTact);
    }

    public void waitUntilProcessorsAvailable() {
        while (g.vertexSet().stream().anyMatch(processor -> !processor.isAvailable())) {
            runTact();
        }
    }
    public Integer getOperationCost(Expression expression) {
        if (expression.getNode().equals("+")) {
            return 1;
        } else if (expression.getNode().equals("-")) {
            return 2;
        } else if (expression.getNode().equals("*")) {
            return 5;
        } else if (expression.getNode().equals("/")) {
            return 8;
        } else if (functionCosts.containsKey(expression.getNode())) {
            return functionCosts.get(expression.getNode());
        }
        throw new RuntimeException("Cannot get cost for expression: " + expression);
    }

    public void printDiagram() {
        AsciiTable at = new AsciiTable();
        var processors = new ArrayList<>(g.vertexSet().stream().toList());
        //processors.sort(Comparator.co(Expression::getLevel));

        at.addRule();
        var header = new ArrayList<String>();
        header.add("Index");
        processors.forEach(processor -> header.add(processor.getName()));
        at.addRow(header);

        for (int i = 0; i < processors.get(0).getOperationPerTact().size(); i++) {
            at.addRule();
            var tactOperations = new ArrayList<String>();
            tactOperations.add(String.valueOf(i));
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
        var systemTacts = processors.get(0).getOperationPerTact().size();
        System.out.println("Quantity of tacts: " + systemTacts);
        System.out.println("Acceleration coefficient: " + ((double) getTotalTactsOnOneProcessor()) / systemTacts);
        System.out.println("Efficiency coefficient: " + getEfficiencyCoefficient(processors));
    }

    public List<Double> getExecutionStats() {
        List<Double> stats = new ArrayList<>();
        var systemTacts = getProcessors().get(0).getOperationPerTact().size();
        stats.add((double) systemTacts);
        stats.add(((double) getTotalTactsOnOneProcessor()) / systemTacts);
        stats.add(getEfficiencyCoefficient(getProcessors()));
        return stats;
    }

    private Integer getTotalTactsOnOneProcessor() {
        AtomicReference<Integer> totalTacts = new AtomicReference<>(0);
        expressionsPerLevel.forEach((level, expressions) ->
                expressions.forEach(e -> totalTacts.updateAndGet(v -> v + getOperationCost(e)))
        );
        return totalTacts.get();
    }

    private Double getEfficiencyCoefficient(List<Processor> processors) {
        var tactsWorking = 0;
        var totalTactsOnAllProcessors = processors.get(0).getOperationPerTact().size() * processors.size();
        for (Processor processor : processors) {
            for (String operation : processor.getOperationPerTact()) {
                if (!operation.isBlank() && (!operation.contains("R") && !operation.contains("W"))) {
                    tactsWorking++;
                }
            }
        }
        return ((double) tactsWorking) / totalTactsOnAllProcessors;
    }
}
