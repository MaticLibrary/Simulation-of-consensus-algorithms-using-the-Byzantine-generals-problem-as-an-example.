package com.example.controller;

import com.example.ApplicationState;
import com.example.draw.CreationHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/view/mainMenuView.fxml")
public class MainMenuController implements ChangeListener<ApplicationState> {
    @Autowired
    GraphController graphController;

    @Autowired
    SimulationController simulationController;

    // Toolbars
    @FXML
    GraphEditController graphToolsController;

    // Draw Menu
    @FXML
    DrawMenuController drawMenuController;

    // File Menu
    @FXML
    FileMenuController fileMenuController;

    // Simulation Menu
    @FXML
    SimulationMenuController simulationMenuController;

    private void bindToolbars(){
        graphToolsController.setDrawMenuController(drawMenuController);
        graphToolsController.setSimulationMenuController(simulationMenuController);
        graphToolsController.setSimulationController(simulationController);
    }

    @FXML
    public void initialize(){
        bindToolbars();
    }

    public void setDrawingHelper(CreationHelper drawingHelper) {
        drawMenuController.setDrawHelper(drawingHelper);
    }

    @Override
    public void changed(ObservableValue<? extends ApplicationState> observable, ApplicationState oldValue, ApplicationState newValue) {
        switch (newValue){
            case SIMULATING -> {
                drawMenuController.setEnabled(false);
                fileMenuController.setImportEnabled(false);
                graphToolsController.setEnabled(false, ApplicationState.DRAWING);
                graphToolsController.setEnabled(true, ApplicationState.SIMULATING);

                simulationController.setSettingsValidation(graphController);
            }
            case DRAWING -> {
                drawMenuController.setEnabled(true);
                fileMenuController.setImportEnabled(true);
                graphToolsController.setEnabled(true, ApplicationState.DRAWING);
                graphToolsController.setEnabled(false, ApplicationState.SIMULATING);
            }
        }
    }
}
