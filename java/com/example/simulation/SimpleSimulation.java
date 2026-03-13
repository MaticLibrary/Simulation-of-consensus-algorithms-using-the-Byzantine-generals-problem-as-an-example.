package com.example.simulation;

import com.example.algorithm.Algorithm;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.animation.AnimationEngine;
import com.example.controller.GraphController;
import com.example.engines.InformationEngine;
import com.example.settings.AlgorithmSettings;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Setter;

import java.util.concurrent.CountDownLatch;

public class SimpleSimulation implements Simulation {

    private Algorithm algorithm;
    private AlgorithmSettings settings;
    private final AnimationEngine animationEngine;
    @Setter
    private InformationEngine informationEngine;
    private final BooleanProperty allowAnimations = new SimpleBooleanProperty(true);

    @Setter
    private GraphController graphController;

    public SimpleSimulation(GraphController graphController) {
        this.graphController = graphController;
        this.animationEngine = new AnimationEngine(graphController);
    }

    public void setEnvironment(Algorithm algorithm, AlgorithmSettings settings) {
        this.algorithm = algorithm;
        this.settings = settings;
        this.animationEngine.setGraphController(graphController);
    }

    @Override
    public void allowAnimations(boolean allow) {
        allowAnimations.setValue(allow);
    }

    public void loadEnvironment() {
        algorithm.loadEnvironment(graphController.getGraph(), settings);
    }

    public BooleanProperty getIsFinishedProperty() {
        return algorithm.getIsFinishedProperty();
    }

    public StepReport step() {
        StepReport report;
        if (allowAnimations.get()) {
            graphController.removeAllVerticesListeners();
            runOnFxThreadAndWait(() -> graphController.enableGraphInteractions(false));
            report = algorithm.step();
            if (report == null) {
                runOnFxThreadAndWait(() -> graphController.enableGraphInteractions(true));
                runOnFxThreadAndWait(graphController::update);
                return null;
            }
            if (informationEngine != null)
                informationEngine.processReport(report);
            animationEngine.animate(report);
            graphController.addAllVerticesListeners();
            runOnFxThreadAndWait(() -> graphController.enableGraphInteractions(true));
            // update coloring if something didn't change in animations
            graphController.getGraph().vertices().forEach(vertex -> graphController.colorVertex(vertex));
        }
        else {
            report = algorithm.step();
        }
        if (report == null) {
            runOnFxThreadAndWait(graphController::update);
            return null;
        }
        if (algorithm.isFinished()) {
            removeSimulationRelatedColoring();
        }
        runOnFxThreadAndWait(graphController::update);
        return report;
    }

    @Override
    public void setAnimationsSpeed(double speedMultiplier) {
        animationEngine.setAnimationsSpeed(speedMultiplier);
    }

    public void removeSimulationRelatedColoring() {
        graphController.getGraph()
                .vertices()
                .forEach(v -> graphController.highlightRole(v, VertexRole.NONE));
    }

    public boolean isGraphEmpty() {
        return graphController.getGraph().vertices().isEmpty();
    }

    @Override
    public void clearData() {
        graphController.clearVerticesTooltips();
    }

    private void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
