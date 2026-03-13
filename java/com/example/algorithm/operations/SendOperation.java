package com.example.algorithm.operations;

import com.brunomnsilva.smartgraph.graph.Vertex;
import javafx.beans.property.BooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SendOperation implements Operation{
    @Getter
    private Vertex<Integer> from;
    @Getter
    private Vertex<Integer> to;
    @Getter
    private BooleanProperty sentOpinion;

    @Override
    public OperationType getType() {
        return OperationType.SEND;
    }

    @Override
    public String getDescription() {
        return "Wierzchołek " + from.element().toString() + " wysyła wiadomość: " + (sentOpinion.get() ? "atak" : "odwrót") + " do " + to.element().toString();
    }
}
