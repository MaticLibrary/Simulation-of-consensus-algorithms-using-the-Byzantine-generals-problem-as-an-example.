package com.example.draw;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.*;
import com.example.util.DrawMouseEventHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

import java.net.URI;
import java.util.function.Consumer;

public class MySmartGraphPanel<V,E> extends SmartGraphPanel<V,E> {
    @Getter
    private BooleanProperty ableToInit = new SimpleBooleanProperty(false);

    public MySmartGraphPanel(Graph<V, E> theGraph) {
        super(theGraph);
        bindInitialized();
//        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);
    }

    public MySmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties) {
        super(theGraph, properties);
        bindInitialized();
//        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);
    }

    public MySmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        super(theGraph, placementStrategy);
        bindInitialized();
//        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);
    }

    public MySmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties, SmartPlacementStrategy placementStrategy) {
        super(theGraph, properties, placementStrategy);
        bindInitialized();
//        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);
    }

    public MySmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties, SmartPlacementStrategy placementStrategy, URI cssFile) {
        super(theGraph, properties, placementStrategy, cssFile);
        bindInitialized();
//        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);
    }

    private void bindInitialized(){
        ableToInit.bind(Bindings.and(widthProperty().greaterThan(0.0), heightProperty().greaterThan(0.0)));
    }

    public void setVertexSingleClickAction(Consumer<SmartGraphVertex<V>> action) {
//        this.setOnMouseClicked((mouseEvent) -> {
//            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 1 && mouseEvent.getEventType() != MouseEvent.MOUSE_DRAGGED) {
//                Node node = UtilitiesJavaFX.pick(this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
//                if (node == null) {
//                    return;
//                }
//
//                if (node instanceof SmartGraphVertex) {
//                    SmartGraphVertex v = (SmartGraphVertex)node;
//                    action.accept(v);
//                }
//            }
//        });
        DrawMouseEventHandler drawMouseEventHandler = new DrawMouseEventHandler();
        drawMouseEventHandler.setOnClickedEventHandler((mouseEvent) -> {
            Node node = UtilitiesJavaFX.pick(this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            if (node == null) {
                return;
            }

            if (node instanceof SmartGraphVertex) {
                SmartGraphVertex v = (SmartGraphVertex)node;
                action.accept(v);
            }
        });
        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);

    }

    public void setEdgeSingleClickAction(Consumer<SmartGraphEdge<E,V>> action){
        DrawMouseEventHandler drawMouseEventHandler = new DrawMouseEventHandler();
        drawMouseEventHandler.setOnClickedEventHandler((mouseEvent) -> {
            Node node = UtilitiesJavaFX.pick(this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            if (node == null) {
                return;
            }

            if (node instanceof SmartGraphEdge) {
                SmartGraphEdge e = (SmartGraphEdge)node;
                action.accept(e);
            }
        });
        this.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);
    }
}
