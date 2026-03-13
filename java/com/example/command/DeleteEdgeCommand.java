package com.example.command;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.example.controller.GraphController;

import java.util.Collection;

public class DeleteEdgeCommand implements Command {

    private GraphController graphController;
    private Integer firstVertexIndex;
    private Integer secondVertexIndex;

    public DeleteEdgeCommand(GraphController graphController, Edge<Integer, Integer> edge) {
        this.graphController = graphController;
        var vertices = edge.vertices();
        this.firstVertexIndex = vertices[0].element();
        this.secondVertexIndex = vertices[1].element();
    }

    @Override
    public void execute() {
        Collection<Edge<Integer, Integer>> foundEdges =
                graphController.getGraph().edgesBetween(firstVertexIndex, secondVertexIndex);
        graphController.getGraph().removeEdge(foundEdges.iterator().next());
        graphController.update();
    }

    @Override
    public void undo() {
        graphController.getGraph().insertEdge(firstVertexIndex, secondVertexIndex, 1);
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
