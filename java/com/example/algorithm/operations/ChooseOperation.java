package com.example.algorithm.operations;

import com.brunomnsilva.smartgraph.graph.Vertex;
import javafx.beans.property.BooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ChooseOperation implements Operation {
    @Getter
    private Vertex<Integer> vertex;
    @Getter
    private BooleanProperty chosenOpinion;

    @Override
    public OperationType getType() {
        return OperationType.CHOOSE;
    }

    @Override
    public String getDescription() {
        return "Wierzchołek " + vertex.element().toString() + " decyduje: " + (chosenOpinion.get() ? "atak" : "odwrót");
    }

}
