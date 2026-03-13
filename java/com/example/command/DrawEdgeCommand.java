package com.example.command;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.controller.GraphController;

import java.util.Collection;

public class DrawEdgeCommand implements Command {

    private GraphController graphController;
    private Integer firstVertexIndex;
    private Integer secondVertexIndex;

    public DrawEdgeCommand(GraphController graphController, Vertex<Integer> firstVertex, Vertex<Integer> secondVertex) {
        this.graphController = graphController;
        this.firstVertexIndex = firstVertex.element();
        this.secondVertexIndex = secondVertex.element();
    }

    @Override
    public void execute() {
        graphController.getGraph().insertEdge(firstVertexIndex, secondVertexIndex, 1);
        graphController.update();
    }

    @Override
    public void undo() {
        Collection<Edge<Integer, Integer>> foundEdges =
                graphController.getGraph().edgesBetween(firstVertexIndex, secondVertexIndex);
        graphController.getGraph().removeEdge(foundEdges.iterator().next());
        graphController.update();
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getName() {
        return null;
    }
}
