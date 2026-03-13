package com.example.command;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.controller.GraphController;

public class DrawVertexCommand implements Command {

    private GraphController graphController;
    private Integer vertexIndex;
    private double x;
    private double y;

    public DrawVertexCommand(GraphController graphController, double x, double y) {
        this.graphController = graphController;
        this.vertexIndex = graphController.getNextVertexId();
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute() {
        Vertex<Integer> vertex = graphController.getGraph().insertVertex(vertexIndex);
        graphController.update();
        graphController.setVertexPosition(vertex, x, y);
        graphController.colorVertex(vertex);
        graphController.onAddVertex(vertex);
    }

    @Override
    public void undo() {
        Vertex<Integer> foundVertex = graphController.getGraph().getVertexByKey(vertexIndex);
        graphController.getGraph().removeVertex(foundVertex);
        graphController.onRemoveVertex(foundVertex);
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
