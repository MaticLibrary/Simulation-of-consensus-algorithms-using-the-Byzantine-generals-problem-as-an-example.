package com.example.controller;

import com.example.ApplicationState;
import com.example.algorithm.AlgorithmType;
import com.example.draw.CreationHelper;
import com.example.model.MyGraph;
import com.example.simulation.SimpleSimulation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/view/appView.fxml")
public class AppController {

    @FXML
    private CheckBox automaticLayout;
    @FXML
    private BorderPane root;

    @FXML
    private GraphController graphController;

    @FXML
    private MainMenuController menuController;

    @FXML
    @Getter
    private SimulationController simulationController;

    @FXML
    private StatisticsController statisticsController;

    @Setter
    @Getter
    private ObjectProperty<ApplicationState> applicationStateProperty =
            new SimpleObjectProperty<>();

    private CreationHelper drawingHelper = new CreationHelper();
    private boolean simulationInitialized = false;

    public BorderPane getRoot() {
        return root;
    }

    public void initGraph() {
        graphController.setModelGraph(new MyGraph<>());
//        initDrawingHelper();
        applicationStateProperty.addListener(menuController);
        initSimulationController();
        setApplicationState(ApplicationState.DRAWING);
    }

    public void initDrawingHelper() {
        graphController.removeObserver(drawingHelper);
        drawingHelper = new CreationHelper();
        drawingHelper.setGraphController(graphController);
        graphController.addObserver(drawingHelper);
        menuController.setDrawingHelper(drawingHelper);
    }

    public void initSimulationController() {
        if (simulationInitialized) {
            return;
        }
        simulationController.setSimulation(new SimpleSimulation(graphController));
        simulationController.setAvailableAlgorithms(FXCollections.observableArrayList(
                AlgorithmType.LAMPORT,
                AlgorithmType.KING,
                AlgorithmType.PBFT,
                AlgorithmType.PRIVATE_BFT
        ));
        simulationInitialized = true;
    }

    public void setApplicationState(ApplicationState applicationState) {
        this.applicationStateProperty.set(applicationState);
        switch (applicationState) {
            case DRAWING -> enterDrawingState();
            case SIMULATING -> enterSimulatingState();
        }
    }

    private void enterDrawingState() {
        simulationController.stop();
        initDrawingHelper();
        simulationController.show();
        simulationController.clearInformation();
        graphController.clearVerticesTooltips();
    }

    private void enterSimulatingState() {
        initSimulationController();
        graphController.removeObserver(drawingHelper);
        simulationController.show();
    }


    public ApplicationState getApplicationState() {
        return applicationStateProperty.get();
    }
}
