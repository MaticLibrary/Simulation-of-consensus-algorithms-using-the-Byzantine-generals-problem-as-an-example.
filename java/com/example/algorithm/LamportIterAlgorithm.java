package com.example.algorithm;

import com.example.algorithm.operations.ChooseOperation;
import com.example.algorithm.operations.SendOperation;
import com.example.algorithm.report.OperationsBatch;
import com.example.algorithm.report.StepReport;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import com.example.settings.AlgorithmSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LamportIterAlgorithm implements Algorithm {
    private MyGraph<Integer, Integer> graph;
    private Stack<StackRecord> stack = new Stack<>();
    private final BooleanProperty isFinished = new SimpleBooleanProperty(false);
    private final List<MyVertex<Integer>> verticesWithOpinion = new ArrayList<>();

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.LAMPORT;
    }

    private StepReport om_iter() {
        var record = stack.pop();
        LamportIterStepReport stepReport = new LamportIterStepReport();

        stepReport.fillProperties(record);
        stepReport.fillRoles(record);
        stepReport.setAlgorithmPhase(record.phase);
        OperationsBatch firstOperationBatch = new OperationsBatch();
        OperationsBatch secondOperationBatch = new OperationsBatch();

        switch (record.phase) {
            case SEND -> {
                for (MyVertex<Integer> vertex : record.lieutenants) {
                    BooleanProperty commanderOpinion = record.commander.getNextOpinion(vertex);
                    vertex.receiveOpinion(commanderOpinion);
                    firstOperationBatch.add(new SendOperation(record.commander, vertex, commanderOpinion));
                    if (!verticesWithOpinion.contains(vertex)) {
                        secondOperationBatch.add(new ChooseOperation(vertex, commanderOpinion));
                        vertex.setIsSupporting(record.commander.getNextOpinion(vertex).getValue());
                        verticesWithOpinion.add(vertex);
                    }
                }

                if (record.m > 0) {
                    record.previous_commanders.add(record.commander);
                    stack.push(new StackRecord(record.commander, new ArrayList<>(record.previous_commanders), record.lieutenants, record.m, AlgorithmPhase.CHOOSE));

                    for (MyVertex<Integer> vertex : record.lieutenants) {
                        List<MyVertex<Integer>> lieutenants = getLieutenants(vertex, record.previous_commanders);
                        if (!lieutenants.isEmpty()) {
                            stack.push(new StackRecord(vertex, new ArrayList<>(record.previous_commanders), lieutenants, record.m - 1, AlgorithmPhase.SEND));
                        }
                    }
                }
            }
            case CHOOSE -> {
                for (MyVertex<Integer> vertex : record.lieutenants) {
                    vertex.chooseMajority();
                    firstOperationBatch.add(new ChooseOperation(vertex, vertex.getIsSupporting()));
                }
            }
        }
        stepReport.addBatch(firstOperationBatch);
        if (!secondOperationBatch.getOperations().isEmpty()) {
            stepReport.addBatch(secondOperationBatch);
        }
        stepReport.setNumSupporting(graph.getSupportingOpinionCount());
        stepReport.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        return stepReport;
    }

    private List<MyVertex<Integer>> getLieutenants(MyVertex<Integer> vertex, List<MyVertex<Integer>> commanders) {
        return graph.vertexNeighbours(vertex).stream()
                .filter(v -> !commanders.contains((MyVertex<Integer>) v))
                .map(v -> (MyVertex<Integer>) v)
                .toList();
    }

    @Override
    public void loadEnvironment(MyGraph<Integer, Integer> graph, AlgorithmSettings settings) {
        this.graph = graph;
        stack = new Stack<>();
        verticesWithOpinion.clear();
        int depth = (int) settings.getSettings().get("depth").getValue();
        if (graph.numVertices() > 0) {
            MyVertex<Integer> commander = (MyVertex<Integer>) graph.vertices().stream().toList().get(0);
            stack.push(new StackRecord(commander,
                    new ArrayList<>(),
                    graph.vertexNeighbours(commander).stream().map(vertex -> (MyVertex<Integer>) vertex).toList(),
                    depth, AlgorithmPhase.SEND));
        }
    }

    @Override
    public StepReport step() {
        if (!stack.empty()) {
            StepReport stepReport = om_iter();
            checkIsFinished();
            stepReport.setNumSupporting(graph.getSupportingOpinionCount());
            stepReport.setNumNotSupporting(graph.getNotSupportingOpinionCount());
            return stepReport;
        }
        return null;
    }

    @Override
    public boolean isFinished() {
        return isFinished.get();
    }

    @Override
    public BooleanProperty getIsFinishedProperty() {
        return isFinished;
    }

    private void checkIsFinished() {
        if (stack.empty()) {
            isFinished.set(true);
        }
    }

    private class LamportIterStepReport extends StepReport {
        public void fillRoles(StackRecord record) {
            getRoles().put(record.commander, VertexRole.COMMANDER);
            record.lieutenants.forEach(vertex -> getRoles().put(vertex, VertexRole.LIEUTENANT));
            graph.vertices()
                    .stream()
                    .filter(vertex -> !vertex.equals(record.commander) && !record.lieutenants.contains((MyVertex<Integer>) vertex))
                    .forEach(vertex -> getRoles().put(vertex, VertexRole.NONE));
        }

        public void fillProperties(StackRecord record) {
            getProperties().put("depth", String.valueOf(record.m));
            getProperties().put("phase", record.phase.toString());
        }
    }

    private record StackRecord(MyVertex<Integer> commander,
                               List<MyVertex<Integer>> previous_commanders,
                               List<MyVertex<Integer>> lieutenants,
                               int m,
                               AlgorithmPhase phase) {
    }
}
