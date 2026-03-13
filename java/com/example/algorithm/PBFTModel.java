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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PBFTModel implements Algorithm {
    private final Random random = new Random();
    private MyGraph<Integer, Integer> graph;
    private MyVertex<Integer> selectedAgent;
    private AlgorithmPhase algorithmPhase = AlgorithmPhase.SEND;
    private ProbabilityType probabilityType = ProbabilityType.AUTO;
    private int q;
    private int maxTimeout;
    private int time = 0;
    private final BooleanProperty isFinished = new SimpleBooleanProperty(false);

    public PBFTModel() {}

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.PBFT;
    }

    @Override
    public void loadEnvironment(MyGraph<Integer, Integer> graph, AlgorithmSettings settings) {
        this.graph = graph;
        this.maxTimeout = (int) settings.getSettings().get("time").getValue();
        this.q = (int) settings.getSettings().get("q").getValue();
        this.probabilityType = (ProbabilityType) settings.getSettings().get("probability").getValue();
    }

    @Override
    public StepReport step() {
        return algorithmPhase == AlgorithmPhase.SEND ? performSendOperation() : performChooseOperation();
    }

    private StepReport performSendOperation() {
        algorithmPhase = AlgorithmPhase.CHOOSE;
        return sendOpinions();
    }

    private StepReport performChooseOperation() {
        algorithmPhase = AlgorithmPhase.SEND;
        StepReport report = makeDecision();
        time++;
        checkIsFinished();
        return report;
    }

    private StepReport sendOpinions() {
        PBFTStepReport report = new PBFTStepReport();
        selectedAgent = selectRandomAgent();
        List<Vertex<Integer>> agentNeighbours = selectRandomNeighbours(selectedAgent);

        report.fillRoles(selectedAgent, agentNeighbours);
        report.addBatch(createSendOperationsBatch(agentNeighbours));
        updateStepReport(report, AlgorithmPhase.SEND);
        return report;
    }

    private StepReport makeDecision() {
        PBFTStepReport report = new PBFTStepReport();
        report.fillRoles(selectedAgent, null);

        if (shouldAcceptNeighboursOpinion()) {
            selectedAgent.setIsSupporting(selectedAgent.getMajorityVote());
        }
        OperationsBatch operationsBatch = new OperationsBatch();
        operationsBatch.add(new ChooseOperation(selectedAgent, selectedAgent.getIsSupporting()));

        report.addBatch(operationsBatch);
        updateStepReport(report, AlgorithmPhase.CHOOSE);
        selectedAgent.clearKnowledge();
        return report;
    }

    private void updateStepReport(PBFTStepReport report, AlgorithmPhase phase) {
        report.setNumSupporting(graph.getSupportingOpinionCount());
        report.setNumNotSupporting(graph.getNotSupportingOpinionCount());
        report.setAlgorithmPhase(phase);
        report.getProperties().put("time", String.valueOf(time));
        report.getProperties().put("probability", String.valueOf(getProbability()));
    }

    private MyVertex<Integer> selectRandomAgent() {
        int agentIndex = random.nextInt(graph.numVertices());
        return (MyVertex<Integer>) graph.vertices().stream().toList().get(agentIndex);
    }

    private List<Vertex<Integer>> selectRandomNeighbours(Vertex<Integer> vertex) {
        List<Vertex<Integer>> neighbours = new ArrayList<>(graph.vertexNeighbours(vertex));
        List<Vertex<Integer>> selectedNeighbours = new ArrayList<>();
        for (int i = 0; i < q && !neighbours.isEmpty(); i++) {
            int randomIndex = random.nextInt(neighbours.size());
            selectedNeighbours.add(neighbours.remove(randomIndex));
        }
        return selectedNeighbours;
    }

    private OperationsBatch createSendOperationsBatch(List<Vertex<Integer>> agentNeighbours) {
        OperationsBatch operationsBatch = new OperationsBatch();
        agentNeighbours.forEach(neighbour -> {
            BooleanProperty opinion = ((MyVertex<Integer>) neighbour).getNextOpinion(selectedAgent);
            selectedAgent.receiveOpinion(opinion);
            operationsBatch.add(new SendOperation(neighbour, selectedAgent, opinion));
        });
        return operationsBatch;
    }

    private boolean shouldAcceptNeighboursOpinion() {
        return selectedAgent.getKnowledge().stream().distinct().count() <= 1 || checkProbability();
    }

    private boolean checkProbability() {
        return random.nextDouble() <= getProbability();
    }

    private double getProbability() {
        if (maxTimeout <= 0) {
            return 1.0;
        }
        return time / (double) maxTimeout;
    }

    private void checkIsFinished() {
        isFinished.setValue(time >= maxTimeout);
    }

    @Override
    public boolean isFinished() {
        return isFinished.get();
    }

    @Override
    public BooleanProperty getIsFinishedProperty() {
        return isFinished;
    }

    private class PBFTStepReport extends StepReport {
        public void fillRoles(Vertex<Integer> agent, List<Vertex<Integer>> neighbours) {
            for (Vertex<Integer> v : graph.vertices()) {
                getRoles().put(v, determineRole(v, agent, neighbours));
            }
        }

        private VertexRole determineRole(Vertex<Integer> vertex, Vertex<Integer> agent, List<Vertex<Integer>> neighbours) {
            if (vertex.equals(agent)) {
                return VertexRole.VOTER;
            } else if (neighbours != null && neighbours.contains(vertex)) {
                return VertexRole.NEIGHBOUR;
            }
            return VertexRole.NONE;
        }
    }
}
