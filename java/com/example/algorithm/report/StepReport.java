package com.example.algorithm.report;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.algorithm.AlgorithmPhase;
import com.example.algorithm.VertexRole;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepReport {
    @Getter
    private Map<Vertex<Integer>, VertexRole> roles = new HashMap<>();
    @Getter
    private List<OperationsBatch> operationsBatches = new ArrayList<>();
    @Getter
    private Map<String, String> properties = new HashMap<>();
    @Getter
    @Setter
    private int numSupporting;
    @Getter
    @Setter
    private int numNotSupporting;
    @Getter
    @Setter
    private AlgorithmPhase algorithmPhase;

    public void addBatch(OperationsBatch operations) {
        operationsBatches.add(operations);
    }

    public void removeBatch(OperationsBatch operations) {
        operationsBatches.remove(operations);
    }
}
