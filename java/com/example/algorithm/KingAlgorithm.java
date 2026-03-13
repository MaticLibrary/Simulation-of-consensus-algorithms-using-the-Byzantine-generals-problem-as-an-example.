package com.example.algorithm;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.algorithm.operations.ChooseOperation;
import com.example.algorithm.operations.SendOperation;
import com.example.algorithm.report.OperationsBatch;
import com.example.algorithm.report.StepReport;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import com.example.settings.AlgorithmSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;


public class KingAlgorithm implements Algorithm {
    private int phase = 1;
    private int numberOfPhases;
    private MyGraph<Integer, Integer> graph;
    private AlgorithmPhase round = AlgorithmPhase.SEND;

    @Getter
    private final BooleanProperty isFinished = new SimpleBooleanProperty(false);

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.KING;
    }

    @Override
    public void loadEnvironment(MyGraph<Integer, Integer> graph, AlgorithmSettings settings) {
        this.graph = graph;
        numberOfPhases = (int) settings.getSettings().get("phase").getValue();
    }

    @Override
    public StepReport step() {
        switch (round) {
            case SEND -> {
                round = AlgorithmPhase.CHOOSE;
                StepReport stepReport = firstRound();
                return addAlgorithmPhaseToReport(stepReport, AlgorithmPhase.SEND);
            }
            case CHOOSE -> {
                round = AlgorithmPhase.SEND;
                checkIsFinished();
                StepReport stepReport = secondRound();
                phase++;
                return addAlgorithmPhaseToReport(stepReport, AlgorithmPhase.CHOOSE);
            }
        }
        return null;
    }

    private StepReport addAlgorithmPhaseToReport(StepReport stepReport, AlgorithmPhase algorithmPhase) {
        stepReport.setAlgorithmPhase(algorithmPhase);
        return stepReport;
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
        if (phase == numberOfPhases) {
            isFinished.setValue(true);
        }
    }

    public StepReport firstRound() {
        System.out.println("First round");
        KingStepReport report = new KingStepReport();
        report.fillRoles(null);
        OperationsBatch firstOperationsBatch = new OperationsBatch();
        OperationsBatch secondOperationBatch = new OperationsBatch();
        cleanKnowledge();
        for (Vertex<Integer> v : graph.vertices()) {
            // send opinion to itself, to mention it in knowledge table
            BooleanProperty vOpinion = ((MyVertex<Integer>) v).isSupportingOpinion();
            ((MyVertex<Integer>) v).receiveOpinion(vOpinion);
            for (Vertex<Integer> u : graph.vertexNeighbours(v)) {
                BooleanProperty opinion = ((MyVertex<Integer>) v).getNextOpinion((MyVertex<Integer>) u);
                ((MyVertex<Integer>) u).receiveOpinion(opinion);
                firstOperationsBatch.add(new SendOperation(v, u, opinion));
            }
        }
        for (Vertex<Integer> v : graph.vertices()) {
            ((MyVertex<Integer>) v).chooseMajority();
            secondOperationBatch.add(new ChooseOperation(v, ((MyVertex<Integer>) v).getIsSupporting()));
        }
        report.addBatch(firstOperationsBatch);
        report.addBatch(secondOperationBatch);
        report.setNumSupporting(graph.getSupportingOpinionCount());
        report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        report.getProperties().put("phase", String.valueOf(phase));
        report.getProperties().put("round", String.valueOf(1));
        return report;
    }

    public StepReport secondRound() {
        System.out.println("Second round");
        KingStepReport report = new KingStepReport();
        MyVertex<Integer> king = (MyVertex<Integer>) graph.vertices().stream().toList().get((phase-1) % graph.numVertices());
        report.fillRoles(king);
        int condition = graph.numVertices() / 2 + allowedNumberOfTraitors();
        OperationsBatch firstOperationsBatch = new OperationsBatch();
        OperationsBatch secondOperationBatch = new OperationsBatch();
        for (Vertex<Integer> v : graph.vertexNeighbours(king)) {
            BooleanProperty kingOpinion = king.getNextOpinion((MyVertex<Integer>) v);
            firstOperationsBatch.add(new SendOperation(king, v, kingOpinion));
            ((MyVertex<Integer>) v).chooseMajorityWithTieBreaker(kingOpinion, condition);
            secondOperationBatch.add(new ChooseOperation(v, ((MyVertex<Integer>) v).getIsSupporting()));
        }
        report.addBatch(firstOperationsBatch);
        report.addBatch(secondOperationBatch);
        report.setNumSupporting(graph.getSupportingOpinionCount());
        report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        report.getProperties().put("phase", String.valueOf(phase));
        report.getProperties().put("round", String.valueOf(2));
        report.getProperties().put("accept king opinion condition", String.valueOf(condition));
        return report;
    }

    private void cleanKnowledge() {
        graph.vertices().forEach(vertex -> ((MyVertex<Integer>)vertex).clearKnowledge());
    }

    private int allowedNumberOfTraitors() {
        return (int)Math.ceil((double)graph.numVertices()/4.0)-1;
    }

    private class KingStepReport extends StepReport {
        public void fillRoles(Vertex<Integer> king) {
            for (Vertex<Integer> v : graph.vertices()) {
                if (v.equals(king)) {
                    getRoles().put(v, VertexRole.KING);
                } else {
                    getRoles().put(v, VertexRole.NONE);
                }
            }
        }
    }

}
