package com.example.controller;

import com.example.ApplicationState;
import com.example.controller.settings.TraitorSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.application.Platform;

@Component
@FxmlView("/view/simulationMenuView.fxml")
public class SimulationMenuController {

    @FXML
    public MenuItem startItem;
    @FXML
    public MenuItem nextStepItem;
    @FXML
    public MenuItem liveItem;
    @FXML
    public MenuItem instantFinishItem;
    @FXML
    public MenuItem pauseItem;
    @FXML
    public MenuItem simulateItem;
    @FXML
    public MenuItem stopItem;
    @FXML
    public MenuItem traitorSettings;

    @Getter
    private final AppController appController;

    @Autowired
    private SimulationController simulationController;

    @Autowired
    public SimulationMenuController(AppController appController) {
        this.appController = appController;
    }

    public void changeApplicationState() {
        switch (appController.getApplicationState()) {
            case DRAWING -> changeApplicationState(ApplicationState.SIMULATING);
            case SIMULATING -> changeApplicationState(ApplicationState.DRAWING);
        }
    }

    public void changeApplicationState(ApplicationState applicationState) {
        appController.setApplicationState(applicationState);
    }

    @FXML
    public void initialize(){
        bindItems();
        startItem.setOnAction(e -> {
            SimulationController controller = getSimulationControllerSafe();
            if (controller != null) {
                controller.initSimulation();
            }
        });
        nextStepItem.setOnAction(e -> {
            SimulationController controller = getSimulationControllerSafe();
            if (controller != null) {
                controller.doStep();
            }
        });
        liveItem.setOnAction(e -> {
            SimulationController controller = getSimulationControllerSafe();
            if (controller != null) {
                controller.live();
            }
        });
        instantFinishItem.setOnAction(e -> {
            SimulationController controller = getSimulationControllerSafe();
            if (controller != null) {
                controller.instantFinish();
            }
        });
        pauseItem.setOnAction(e -> {
            SimulationController controller = getSimulationControllerSafe();
            if (controller != null) {
                controller.pause();
            }
        });
        stopItem.setOnAction(e -> {
            SimulationController controller = getSimulationControllerSafe();
            if (controller != null) {
                controller.stop();
            }
        });

        traitorSettings.setOnAction(e -> {
            TraitorSettings.setTraitorsAlwaysLie(!TraitorSettings.isTraitorsAlwaysLie());
            traitorSettings.setText(TraitorSettings.isTraitorsAlwaysLie()
                    ? "[Tryb] Zdrajcy zawsze kłamią"
                    : "[Tryb] Zdrajcy kłamią losowo");
        });
        traitorSettings.setText(TraitorSettings.isTraitorsAlwaysLie()
                ? "[Tryb] Zdrajcy zawsze kłamią"
                : "[Tryb] Zdrajcy kłamią losowo");
    }

    public void bindItems() {
        SimulationController controller = appController.getSimulationController();
        if (controller == null) {
            controller = simulationController;
        }
        if (controller == null) {
            Platform.runLater(this::bindItems);
            return;
        }
        ObjectProperty<ApplicationState> applicationState = appController.getApplicationStateProperty();
        BooleanBinding isNotSimulation = applicationState.isEqualTo(ApplicationState.SIMULATING).not();

        startItem.disableProperty().bind(controller.getStartDisabledProperty().or(isNotSimulation));
        nextStepItem.disableProperty().bind(controller.getNextStepDisabledProperty().or(isNotSimulation));
        liveItem.disableProperty().bind(controller.getLiveDisabledProperty().or(isNotSimulation));
        instantFinishItem.disableProperty().bind(controller.getInstantFinishDisabledProperty().or(isNotSimulation));
        pauseItem.disableProperty().bind(controller.getPauseDisabledProperty().or(isNotSimulation));
        stopItem.disableProperty().bind(controller.getStopDisableProperty().or(isNotSimulation));

        traitorSettings.disableProperty().bind(controller.getStartDisabledProperty());
    }

    private SimulationController getSimulationControllerSafe() {
        SimulationController controller = appController.getSimulationController();
        if (controller != null) {
            return controller;
        }
        return simulationController;
    }
}
