package com.example.controller;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.example.algorithm.VertexRole;
import com.example.draw.MySmartGraphPanel;
import com.example.listener.VertexListener;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import com.example.util.DrawMouseEventHandler;
import com.example.util.GraphObserver;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.PopOver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@Component
@FxmlView("/view/graphView.fxml")
public class GraphController {

    @FXML
    private Pane graphRoot;

    @Autowired
    private FxWeaver fxWeaver;

    @Autowired
    private GraphLayoutController graphLayoutController;

    private int vertexIdCounter = 0;
    private Pane container;
    @Getter
    private MySmartGraphPanel<Integer, Integer> graphView;

    @Getter
    private MyGraph<Integer, Integer> graph;
    private List<GraphObserver<Integer, Integer>> observers = new ArrayList<>();
    private Map<Vertex<Integer>, VertexListener> vertexListeners = new HashMap<>();

    public void addObserver(GraphObserver<Integer, Integer> observer) {
        observers.add(observer);
    }

    public void removeObserver(GraphObserver<Integer, Integer> observer) {
        observers.remove(observer);
    }

    public void setModelGraph(MyGraph<Integer, Integer> graph){
        vertexIdCounter = 0;
        for (Vertex<Integer> vertex : graph.vertices()) {
            vertexIdCounter = Integer.max(vertex.element() + 1, vertexIdCounter);
        }
        this.graph = graph;

        //remove old graph
        graphRoot.getChildren().remove(container);
        vertexListeners.clear();
        init();
        initGraphView();

        colorGraphView();
        observers.forEach(observer -> observer.setGraph(graph));
    }

    private void buildGraphContainers() {
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        try {
            URI smartGraphCSS = new ClassPathResource("css/smartgraph.css").getURI();
            ClassPathResource smartGraphPropertiesResource = new ClassPathResource("properties/smartgraph.properties");
            SmartGraphProperties smartGraphProperties = new SmartGraphProperties(smartGraphPropertiesResource.getInputStream());
            graphView = new MySmartGraphPanel<>(graph, smartGraphProperties, strategy, smartGraphCSS);
        } catch (IOException e) {
            graphView = new MySmartGraphPanel<>(graph, null, strategy);
        }
        setGraphViewBindings();
        container = new BorderPane(graphView);
        graphLayoutController.setLayout(graphView, container);
    }

    public void setVertexStyle(int id, String style) {
        Platform.runLater(()->graphView.getStylableVertex(id).setStyleClass(style));
    }

    public void addVertexStyle(int id, String style) {
        Platform.runLater(()->graphView.getStylableVertex(id).addStyleClass(style));
    }

    public void removeVertexStyle(int id, String style) {
        Platform.runLater(()->graphView.getStylableVertex(id).removeStyleClass(style));
    }

    private void setGraphViewBindings(){
        graphView.setVertexSingleClickAction(graphVertex -> {
            observers.forEach(observer -> observer.vertexClicked(graphVertex.getUnderlyingVertex()));
        });

        graphView.setEdgeSingleClickAction(graphEdge -> {
            observers.forEach(observer -> observer.edgeClicked(graphEdge.getUnderlyingEdge()));
        });

        graphView.setVertexDoubleClickAction(graphVertex -> {
            observers.forEach(observer -> observer.vertexDoubleClicked(graphVertex.getUnderlyingVertex()));
            // load popUp view
            FxControllerAndView<VertexSettingsController, Node> controllerAndView = fxWeaver.load(VertexSettingsController.class);
            // bind controller with selected vertex
            controllerAndView.getController().bindVertex((MyVertex<Integer>)graphVertex.getUnderlyingVertex());
            // configure and show popUp
            PopOver vertexSettingsWindow = new PopOver(controllerAndView.getView().get());
            vertexSettingsWindow.show((Node)graphVertex);
        });

        graphView.setEdgeDoubleClickAction(graphEdge -> {
            observers.forEach(observer -> observer.edgeDoubleClicked(graphEdge.getUnderlyingEdge()));
        });

        DrawMouseEventHandler drawMouseEventHandler = new DrawMouseEventHandler();
        drawMouseEventHandler.setOnClickedEventHandler((mouseEvent) -> {
            System.out.println("X = "+mouseEvent.getX());
            System.out.println("Y = "+mouseEvent.getY());
            var x = mouseEvent.getX();
            var y = mouseEvent.getY();
            observers.forEach(observer -> observer.clickedAt(x,y));
        });
        graphView.addEventHandler(MouseEvent.ANY, drawMouseEventHandler);

        graph.vertices().forEach(this::onAddVertex);
    }

    public void onAddVertex(Vertex<Integer> vertex) {
        addVertexListeners(vertex);
        setVertexTooltip(vertex);
    }

    public void onRemoveVertex(Vertex<Integer> vertex) {
        vertexListeners.remove(vertex);
    }

    public void addAllVerticesListeners() {
        graph.vertices().forEach(this::addVertexListeners);
    }

    public void removeAllVerticesListeners() {
        graph.vertices().forEach(this::removeVertexListeners);
    }

    public void addVertexListeners(Vertex<Integer> vertex) {
        VertexListener vertexListener = vertexListeners.get(vertex);
        if (vertexListener == null) {
            vertexListener = new VertexListener((MyVertex<Integer>) vertex, this);
            vertexListeners.put(vertex, vertexListener);
        }
        vertexListener.addTraitorListener();
        vertexListener.addOpinionListener();
    }

    public void removeVertexListeners(Vertex<Integer> vertex) {
        VertexListener vertexListener = vertexListeners.get(vertex);
        if (vertexListener == null)
            throw new IllegalStateException("Can't remove vertex listeners if no listeners were set");
        vertexListener.removeOpinionListener();
        vertexListener.removeTraitorListener();
    }

    public VertexListener getVertexListener(Vertex<Integer> vertex) {
        return vertexListeners.get(vertex);
    }

    public void setVertexTooltip(Vertex<Integer> vertex) {
        graphView.getChildren().stream()
                .filter(n -> n instanceof SmartGraphVertexNode)
                .filter(n -> ((SmartGraphVertexNode<?>) n).getUnderlyingVertex().element().equals(vertex.element()))
                .forEach(n -> {
                    Tooltip t = new Tooltip();
                    t.textProperty().bindBidirectional(((MyVertex<Integer>) vertex).getKnowledgeInfo());
                    Tooltip.install(n, t);
                });
    }

    public void clearVerticesTooltips() {
       graph.vertices().forEach(v -> ((MyVertex<Integer>) v).clearKnowledge());
       updateVerticesTooltips();
    }

    public void updateVerticesTooltips() {
        graph.vertices().forEach(v -> ((MyVertex<Integer>) v).updateKnowledgeInfo());
    }

    public void colorGraphView () {
        for(Vertex<Integer> vertex : graph.vertices()) {
            colorVertex(vertex);
        }
    }

    public void colorVertex (Vertex<Integer> vertex) {
        changeVertexFillStyle(vertex);
        changeVertexStrokeStyle(vertex);
    }

    public void changeVertexFillStyle(Vertex<Integer> vertex) {
        if (((MyVertex<Integer>) vertex).getIsTraitor().get()) {
            removeVertexStyle(vertex.element(), "loyal");
            removeVertexStyle(vertex.element(), "traitor");
            addVertexStyle(vertex.element(), "traitor");
        } else {
            removeVertexStyle(vertex.element(), "traitor");
            removeVertexStyle(vertex.element(), "loyal");
            addVertexStyle(vertex.element(), "loyal");
        }
    }

    public void changeVertexStrokeStyle (Vertex<Integer> vertex) {
        if (((MyVertex<Integer>) vertex).isSupportingOpinion().get()) {
            removeVertexStyle(vertex.element(), "defense");
            removeVertexStyle(vertex.element(), "attack");
            addVertexStyle(vertex.element(), "attack");
        } else {
            removeVertexStyle(vertex.element(), "attack");
            removeVertexStyle(vertex.element(), "defense");
            addVertexStyle(vertex.element(), "defense");
        }
    }

    public void highlightRole (Vertex<Integer> vertex, VertexRole vertexRole) {
        for(VertexRole role : VertexRole.values()){
            removeVertexStyle(vertex.element(), role.toString().toLowerCase(Locale.ROOT));
        }
        addVertexStyle(vertex.element(), vertexRole.toString().toLowerCase(Locale.ROOT));
    }

    public void setVertexPosition(Vertex<Integer> vertex, double x, double y) {
        graphView.setVertexPosition(vertex, x, y);
    }

    public void initGraphView() {
        if (graphView.getAbleToInit().get()) {
            // GraphView is ready to be initialized
            graphView.init();
        }
        else {
            // Listen while GraphView won't be ready
            graphView.getAbleToInit().addListener((o, oldVal, newVal) -> {
                if (newVal && !oldVal) {
                    graphView.init();
                }
            });
        }
    }

    public void addNodeToView(Node node) {
        graphView.getChildren().add(node);
    }

    public Point2D getVertexPosition(Vertex<Integer> vertex) {
        return new Point2D(graphView.getVertexPositionX(vertex), graphView.getVertexPositionY(vertex));
    }

    public void removeNodeFromView(Node node) {
        graphView.getChildren().remove(node);
    }

    // TODO: implement update as listener to graph changes
    public void update() {
        graphView.updateAndWait();
    }

    private void init() {
        buildGraphContainers();
        container.prefWidthProperty().bind(graphRoot.widthProperty());
        container.prefHeightProperty().bind(graphRoot.heightProperty());
        graphRoot.getChildren().add(container);
    }

    public int getNextVertexId() {
        return vertexIdCounter++;
    }

    public void enableGraphInteractions(boolean enable) {
        graphView.setDisable(!enable);
    }

    public BooleanProperty getGraphInteractionsProperty() {
        return graphView.disableProperty();
    }
}
