package com.example.command;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.controller.GraphController;

import java.util.ArrayList;
import java.util.List;

public class DeleteVertexCommand implements Command {

    private GraphController graphController;
    private Integer vertexIndex;
    private double x;
    private double y;
    private List<Integer> removedEdgesVertexIndexes = new ArrayList<>();

    public DeleteVertexCommand(GraphController graphController, Vertex<Integer> vertex) {
        this.graphController = graphController;
        this.vertexIndex = vertex.element();
        this.x = graphController.getVertexPosition(vertex).getX();
        this.y = graphController.getVertexPosition(vertex).getY();
    }

    @Override
    public void execute() {
        Vertex<Integer> foundVertex = graphController.getGraph().getVertexByKey(vertexIndex);
        var edges = graphController.getGraph().incidentEdges(foundVertex);
        for (var edge : edges) {
            var vertices = edge.vertices();
            if (vertices[0].element().equals(vertexIndex)) {
                removedEdgesVertexIndexes.add(vertices[1].element());
            } else {
                removedEdgesVertexIndexes.add(vertices[0].element());
            }
        }
        graphController.getGraph().removeVertex(foundVertex);
        graphController.onRemoveVertex(foundVertex);
        graphController.update();
    }

    @Override
    public void undo() {
        Vertex<Integer> vertex = graphController.getGraph().insertVertex(vertexIndex);
        for (var secondVertexIndex : removedEdgesVertexIndexes) {
            graphController.getGraph().insertEdge(vertexIndex, secondVertexIndex, 1);
        }
        removedEdgesVertexIndexes.clear();
        graphController.update();
        graphController.setVertexPosition(vertex, x, y);
        graphController.colorVertex(vertex);
        graphController.onAddVertex(vertex);
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
