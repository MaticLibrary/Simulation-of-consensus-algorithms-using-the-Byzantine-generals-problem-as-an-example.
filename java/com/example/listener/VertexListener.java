package com.example.listener;

import com.example.controller.GraphController;
import com.example.model.MyVertex;
import javafx.beans.InvalidationListener;

public class VertexListener {
    private MyVertex<Integer> vertex;
    private GraphController graphController;
    private InvalidationListener traitorListener;
    private InvalidationListener opinionListener;

    public VertexListener(MyVertex<Integer> vertex, GraphController graphController) {
        this.vertex = vertex;
        this.graphController = graphController;
        traitorListener = (changed) -> graphController.changeVertexFillStyle(vertex);
        opinionListener = (changed) -> graphController.changeVertexStrokeStyle(vertex);
    }

    public void addTraitorListener() {
        removeTraitorListener();
        vertex.getIsTraitor().addListener(traitorListener);
    }

    public void addOpinionListener() {
        removeOpinionListener();
        vertex.isSupportingOpinion().addListener(opinionListener);
    }

    public void removeTraitorListener() {
        vertex.getIsTraitor().removeListener(traitorListener);
    }

    public void removeOpinionListener() {
        vertex.isSupportingOpinion().removeListener(opinionListener);
    }
}
