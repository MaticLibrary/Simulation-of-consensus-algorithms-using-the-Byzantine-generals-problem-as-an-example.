package com.example.draw;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.command.*;
import com.example.controller.GraphController;
import com.example.util.GraphObserver;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

public class CreationHelper implements GraphObserver<Integer, Integer> {
    @Setter
    @Getter
    private GraphController graphController;
    @Getter
    private DrawMode currentDrawMode = DrawMode.NONE;
    private Vertex<Integer> selectedToEdge;
    @Getter
    private CommandRegistry commandRegistry = new CommandRegistry();

    public void setDrawMode(DrawMode mode){
        if (currentDrawMode != mode){
            cleanCache();
            currentDrawMode = mode;
        }
    }

    private void cleanCache(){
        selectedToEdge = null;
    }

    private boolean checkCreationCondition(DrawMode mode){
        return currentDrawMode == mode;
    }

    @Override
    public void vertexClicked(Vertex<Integer> vertex) {
        System.out.println("SELECT VERTEX WAS CALLED");
        switch (currentDrawMode){
            case EDGE -> {
                if (selectedToEdge == null) {
                    selectedToEdge = vertex;
                }
                else {
                    commandRegistry.executeCommand(new DrawEdgeCommand(graphController, selectedToEdge, vertex));
                    selectedToEdge = null;
                }
            }
            case DELETE -> {
                commandRegistry.executeCommand(new DeleteVertexCommand(graphController, vertex));
            }
        }
    }

    @Override
    public void edgeClicked(Edge<Integer, Integer> edge) {
        System.out.println("SELECT EDGE WAS CALLED");
        switch (currentDrawMode){
            case DELETE -> commandRegistry.executeCommand(new DeleteEdgeCommand(graphController, edge));
        }
    }

    @Override
    public void vertexDoubleClicked(Vertex<Integer> vertex) {

    }

    @Override
    public void edgeDoubleClicked(Edge<Integer, Integer> edge) {

    }

    @Override
    public void clickedAt(double x, double y) {
        switch (currentDrawMode){
            case VERTEX -> {
                // add vertex at clicked position
                commandRegistry.executeCommand(new DrawVertexCommand(graphController, x, y));
            }
        }
    }

    @Override
    public void setGraph(Graph<Integer, Integer> newGraph) {
        commandRegistry.clearStacks();
    }
}
