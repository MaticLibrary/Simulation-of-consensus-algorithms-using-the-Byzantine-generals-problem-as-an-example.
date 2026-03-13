package com.example.controller;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.VertexRole;
import com.example.draw.DefinedGraph;
import com.example.draw.GraphGenerator;
import com.example.draw.TraitorsGenerator;
import com.example.algorithm.operations.Operation;
import com.example.algorithm.report.OperationsBatch;
import com.example.algorithm.report.StepReport;
import com.example.controller.settings.AlgorithmSettingsController;
import com.example.controller.settings.KingSettingsController;
import com.example.controller.settings.LamportSettingsController;
import com.example.controller.settings.PrivateBftSettingsController;
import com.example.controller.settings.pbftSettingsController;
import com.example.engines.InformationEngineFactory;
import com.example.model.MyGraph;
import com.example.simulation.SimpleSimulation;
import com.example.simulation.Simulation;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@FxmlView("/view/simulationOptionsView.fxml")
public class SimulationController {
    @FXML
    public TextFlow warning;
    @FXML
    private VBox parent;
    @FXML
    private LamportSettingsController lamportSettingsController;
    @FXML
    private KingSettingsController kingSettingsController;
    @FXML
    private pbftSettingsController PBFTSettingsController;
    @FXML
    private PrivateBftSettingsController privateBftSettingsController;
    @FXML
    private ComboBox<AlgorithmType> algorithmsBox;
    @FXML
    private Slider animationSpeedSlider;
    @FXML
    private CheckBox skipAnimationsCheckBox;
    @FXML
    private Spinner<Integer> agentsSpinner;
    @FXML
    private Spinner<Integer> traitorsSpinner;
    @FXML
    private Button quickSimulateButton;
    @FXML
    private Label quickSetupHint;
    @FXML
    private InformationController informationController;

    @Getter
    private final BooleanProperty startDisabledProperty = new SimpleBooleanProperty();
    @Getter
    private final BooleanProperty nextStepDisabledProperty = new SimpleBooleanProperty();
    @Getter
    private final BooleanProperty liveDisabledProperty = new SimpleBooleanProperty();
    @Getter
    private final BooleanProperty instantFinishDisabledProperty = new SimpleBooleanProperty();
    @Getter
    private final BooleanProperty pauseDisabledProperty = new SimpleBooleanProperty();
    @Getter
    private final BooleanProperty stopDisableProperty = new SimpleBooleanProperty();

    private Service<?> activeService;
    private Simulation simulation;

    @Autowired
    private StatisticsController statisticsController;

    @Autowired
    private DocumentationController documentationController;

    @Autowired
    private LoggerController loggerController;

    @Autowired
    private GraphController graphController;

    @Autowired
    private GraphGenerator graphGenerator;

    @Autowired
    private TraitorsGenerator traitorsGenerator;

    @Autowired
    private FxWeaver fxWeaver;

    private final BooleanProperty paused = new SimpleBooleanProperty(true);
    private final BooleanProperty started = new SimpleBooleanProperty(false);
    private final BooleanProperty idle = new SimpleBooleanProperty(true);
    private final BooleanProperty isFinished = new SimpleBooleanProperty(false);
    private final BooleanProperty areAlgorithmSettingsValid = new SimpleBooleanProperty(true);
    private final BooleanProperty skipAnimations = new SimpleBooleanProperty(false);

    private static final int DEFAULT_AGENTS = 8;
    private static final int DEFAULT_TRAITORS = 2;

    public void show() {
        parent.setVisible(true);
        parent.setManaged(true);
        initWarning();
    }

    public void hide() {
        parent.setVisible(false);
        parent.setManaged(false);
    }

    public void clearInformation() {
        informationController.clearView();
    }

    public void setSettingsValidation(GraphController graphController) {
        lamportSettingsController.adjustSettingsConditions(graphController.getGraph());
        kingSettingsController.adjustSettingsConditions(graphController.getGraph());
        PBFTSettingsController.adjustSettingsConditions(graphController.getGraph());
        privateBftSettingsController.adjustSettingsConditions(graphController.getGraph());
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        simulation.setAnimationsSpeed(animationSpeedSlider.getValue());
        applyAnimationPreference();
        animationSpeedSlider.valueProperty().addListener(
                observable -> Platform.runLater(
                        () -> simulation.setAnimationsSpeed(animationSpeedSlider.getValue()
                        )
                )
        );
    }

    private void initWarning() {
        MyGraph<Integer, Integer> graph = graphController.getGraph();
        if (graph != null && algorithmsBox.getValue() != AlgorithmType.PBFT) warning.setVisible(
                !graph.isComplete()
        );
        else if (algorithmsBox.getValue() == AlgorithmType.PBFT) warning.setVisible(false);
    }

    @FXML
    public void initialize() {
        hideAlgorithmSettings();
        initQuickSetup();
        initAnimationControls();
        algorithmsBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AlgorithmType algorithmType, boolean empty) {
                super.updateItem(algorithmType, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(algorithmType.toString());
                }
            }
        });

        algorithmsBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        hideAlgorithmSettings();
                        showAlgorithmSettings(newValue);
                        bindStartButtonWithAlgorithmSettings(newValue);
                    }
                }));

        algorithmsBox.getSelectionModel().selectedIndexProperty().addListener(
                (index) -> initWarning()
        );

        nextStepDisabledProperty.setValue(true);
        liveDisabledProperty.setValue(true);
        instantFinishDisabledProperty.setValue(true);
        pauseDisabledProperty.setValue(true);
        stopDisableProperty.setValue(true);

        List<Observable> dependenciesList = new ArrayList<>();
        dependenciesList.add(paused);
        dependenciesList.add(started);
        dependenciesList.add(idle);
        dependenciesList.add(isFinished);
        Observable[] dependencies = dependenciesList.toArray(new Observable[0]);

        nextStepDisabledProperty.bind(Bindings.createBooleanBinding(() ->
                !(idle.get() && started.get() && !isFinished.get()), dependencies));

        liveDisabledProperty.bind(Bindings.createBooleanBinding(() ->
                !(idle.get() && started.get() && !isFinished.get()), dependencies));

        instantFinishDisabledProperty.bind(Bindings.createBooleanBinding(() ->
                !(idle.get() && started.get() && !isFinished.get()), dependencies));

        pauseDisabledProperty.bind(Bindings.createBooleanBinding(() ->
                !(!paused.get() && started.get() && !isFinished.get()), dependencies));

        stopDisableProperty.bind(Bindings.createBooleanBinding(() ->
                !(started.get() && !isFinished.get()), dependencies));
    }

    private void initAnimationControls() {
        if (skipAnimationsCheckBox == null || animationSpeedSlider == null) {
            return;
        }
        skipAnimations.bind(skipAnimationsCheckBox.selectedProperty());
        skipAnimations.addListener((obs, oldVal, newVal) -> applyAnimationPreference());
        animationSpeedSlider.disableProperty().bind(skipAnimations);
    }

    private void applyAnimationPreference() {
        if (simulation == null) {
            return;
        }
        simulation.allowAnimations(!skipAnimations.get());
    }

    private void initQuickSetup() {
        if (agentsSpinner == null || traitorsSpinner == null || quickSimulateButton == null) {
            return;
        }
        agentsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, DEFAULT_AGENTS));
        agentsSpinner.setEditable(true);

        traitorsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, DEFAULT_AGENTS, DEFAULT_TRAITORS));
        traitorsSpinner.setEditable(true);

        agentsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> adjustTraitorSpinner(newVal));
        traitorsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateQuickHint());
        adjustTraitorSpinner(agentsSpinner.getValue());

        quickSimulateButton.setOnAction(e -> runQuickSimulation());
        updateQuickHint();
    }

    private void adjustTraitorSpinner(int agents) {
        if (agents < 1) {
            agents = 1;
        }
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) traitorsSpinner.getValueFactory();
        factory.setMax(agents);
        if (traitorsSpinner.getValue() > agents) {
            factory.setValue(agents);
        }
        updateQuickHint();
    }

    private void updateQuickHint() {
        if (quickSetupHint == null) {
            return;
        }
        int agents = agentsSpinner != null ? agentsSpinner.getValue() : 0;
        int traitors = traitorsSpinner != null ? traitorsSpinner.getValue() : 0;
        int maxByzantine = Math.max(0, (agents - 1) / 2);
        if (traitors > maxByzantine) {
            quickSetupHint.setText("Uwaga: dla BFT wymagane jest N \u2265 2f + 1. Zmniejsz liczb\u0119 zdrajc\u00f3w lub zwi\u0119ksz liczb\u0119 agent\u00f3w.");
        } else {
            quickSetupHint.setText("Tworz\u0119 graf pe\u0142ny (ka\u017cdy z ka\u017cdym) i losowo wybieram zdrajc\u00f3w.");
        }
    }

    private void runQuickSimulation() {
        stop();
        int agents = agentsSpinner.getValue();
        int traitors = Math.max(0, Math.min(traitorsSpinner.getValue(), agents));

        MyGraph<Integer, Integer> generatedGraph = generateFullGraph(agents);
        graphController.setModelGraph(generatedGraph);
        traitorsGenerator.generateTraitorsCount(generatedGraph, traitors);
        graphController.colorGraphView();
        graphController.updateVerticesTooltips();

        setSettingsValidation(graphController);
        initWarning();

        if (algorithmsBox.getValue() == null && algorithmsBox.getItems() != null && !algorithmsBox.getItems().isEmpty()) {
            algorithmsBox.getSelectionModel().select(0);
        }
        initSimulation();
        live();
    }

    private MyGraph<Integer, Integer> generateFullGraph(int agents) {
        Map<String, Integer> settings = new HashMap<>();
        settings.put("numberOfVertices", agents);
        return (MyGraph<Integer, Integer>) graphGenerator.generateGraph(DefinedGraph.FULL, settings);
    }

    private AlgorithmSettingsController getAlgorithmController(AlgorithmType algorithmType) {
        switch (algorithmType) {
            case LAMPORT -> {
                return lamportSettingsController;
            }
            case KING -> {
                return kingSettingsController;
            }
            case PBFT -> {
                return PBFTSettingsController;
            }
            case PRIVATE_BFT -> {
                return privateBftSettingsController;
            }
        }
        return null;
    }

    private void bindStartButtonWithAlgorithmSettings(AlgorithmType algorithmType) {
        areAlgorithmSettingsValid.bind(getAlgorithmController(algorithmType).getAreSettingsValidProperty());
    }

    private void setSimulationFlagsToNotStartedState() {
        paused.unbind();
        started.unbind();
        idle.unbind();
        isFinished.unbind();
        paused.set(true);
        started.set(false);
        idle.set(true);
        isFinished.set(false);
    }

    private void showAlgorithmSettings(AlgorithmType algorithmType) {
        setSettingsManagedAndVisible(algorithmType, true);
    }

    private void hideAlgorithmSettings() {
        hideAlgorithmSettings(AlgorithmType.LAMPORT);
        hideAlgorithmSettings(AlgorithmType.KING);
        hideAlgorithmSettings(AlgorithmType.PBFT);
        hideAlgorithmSettings(AlgorithmType.PRIVATE_BFT);
    }

    private void hideAlgorithmSettings(AlgorithmType algorithmType) {
        setSettingsManagedAndVisible(algorithmType, false);
    }

    private void setSettingsManagedAndVisible(AlgorithmType algorithmType, boolean enable) {
        AlgorithmSettingsController algorithmSettingsController = getAlgorithmController(algorithmType);
        Node settingsParent = algorithmSettingsController.getParent();
        settingsParent.setManaged(enable);
        settingsParent.setVisible(enable);
    }

    public void setAvailableAlgorithms(ObservableList<AlgorithmType> algorithmTypes) {
        algorithmsBox.setItems(algorithmTypes);
        algorithmsBox.getSelectionModel().select(0);

        List<Observable> inputDependencies = List.of(started, isFinished, idle, areAlgorithmSettingsValid);
        Observable[] dependencies = inputDependencies.toArray(new Observable[0]);

        startDisabledProperty.unbind();
        startDisabledProperty.bind(Bindings.createBooleanBinding(() -> {
                    if (simulation.isGraphEmpty() || started.get() || !areAlgorithmSettingsValid.get() || !idle.get()) {
                        return true;
                    }
                    return false;
                }, dependencies
        ));
    }

    public void initSimulation() {
        statisticsController.clear();
        informationController.clearView();
        simulation.clearData();
        applyAnimationPreference();
        AlgorithmType selectedAlgorithm = algorithmsBox.getValue();
        simulation.setEnvironment(selectedAlgorithm.getAlgorithm(), getAlgorithmController(selectedAlgorithm).getAlgorithmSettings());
        simulation.setInformationEngine(InformationEngineFactory.createForAlgorithm(selectedAlgorithm, informationController));
        ((SimpleSimulation) simulation).loadEnvironment();
        isFinished.bind(((SimpleSimulation) simulation).getIsFinishedProperty());
        loggerController.addItem("[Początek] Symulacja uruchomiona: " + selectedAlgorithm + ".");
        statisticsController.addStats(graphController.getGraph().getSupportingOpinionCount(),
                graphController.getGraph().getNotSupportingOpinionCount());
        started.set(true);
    }

    private void processStep() {
        StepReport report = simulation.step();
        if (report == null) {
            return;
        }
        String lastOperation = null;
        for (OperationsBatch operationBatch : report.getOperationsBatches()) {
            loggerController.addItem("");
            for (Operation operation : operationBatch.getOperations()) {
                loggerController.addItem("[Zdarzenie] " + operation.getDescription());
                lastOperation = operation.getDescription();
            }
        }
        if (report.getRoles() != null && !report.getRoles().isEmpty()) {
            List<String> alarms = report.getRoles().entrySet().stream()
                    .filter(entry -> entry.getValue() == VertexRole.ALARM)
                    .map(entry -> entry.getKey().element().toString())
                    .toList();
            if (!alarms.isEmpty()) {
                String alarmMessage = "ALARM: rozbieżność reguł u węzłów " + String.join(", ", alarms) + ".";
                loggerController.addItem("[ALARM] " + alarmMessage);
                lastOperation = alarmMessage;
            }
        }
        statisticsController.setLastOperation(lastOperation);
        statisticsController.addStats(report.getNumSupporting(), report.getNumNotSupporting());
        graphController.updateVerticesTooltips();
    }

    public void openResultDialog() {
        Platform.runLater(() -> {
            Dialog<ButtonType> simulationResultDialog = new Dialog<>();
            FxControllerAndView<SimulationResultController, DialogPane> controllerAndView = fxWeaver.load(SimulationResultController.class);
            controllerAndView.getController().setMessage(graphController.getGraph().checkConsensus());
            simulationResultDialog.setDialogPane(controllerAndView.getView().orElseThrow(() -> new RuntimeException("Can't load dialog view, when there is no present")));
            simulationResultDialog.showAndWait();
        });
    }

    public void doStepTask() {
        if (!isFinished.get()) {
            processStep();

            if (isFinished.get()) {
                onFinish();
            }
        }
    }

    private void liveTask() {
        while (!isFinished.get()) {
            processStep();

            if (paused.get()) {
                return;
            }
        }
        onFinish();
    }

    private void instantFinishTask() {
        while (!isFinished.get()) {
            processStep();

            if (paused.get()) {
                return;
            }
        }
        onFinish();
    }

    public void onFinish() {
        loggerController.addItem("");
        loggerController.addItem("[Koniec] Symulacja zakończona");
        setSimulationFlagsToNotStartedState();
        openResultDialog();
    }

    public void pause() {
        paused.set(true);
    }

    public void live() {
        runService(new SimulationLiveService());
    }

    public void instantFinish() {
        runService(new SimulationInstantFinishService());
    }

    public void doStep() {
        runService(new SimulationStepService());
    }

    private void runService(Service<?> service) {
        activeService = service;
        service.start();
    }

    public void stop() {
        pause();
        if (activeService != null && activeService.isRunning()) {
            activeService.cancel();
        }
        isFinished.unbind();
        if (simulation != null)
            simulation.removeSimulationRelatedColoring();
        setSimulationFlagsToNotStartedState();
    }

    public void openDocumentation(ActionEvent actionEvent) throws IOException {
        documentationController.openDocumentationForAlgorithm(algorithmsBox.getValue());
    }

    public class SimulationLiveService extends Service<Boolean> {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    idle.set(false);
                    paused.set(false);
                    liveTask();
                    idle.set(true);
                    return true;
                }
            };
        }
    }

    public class SimulationStepService extends Service<Boolean> {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    idle.set(false);
                    doStepTask();
                    idle.set(true);
                    return true;
                }
            };
        }
    }

    public class SimulationInstantFinishService extends Service<Boolean> {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    idle.set(false);
                    paused.set(false);
                    simulation.allowAnimations(false);
                    simulation.removeSimulationRelatedColoring();
                    instantFinishTask();
                    applyAnimationPreference();
                    idle.set(true);
                    return true;
                }
            };
        }
    }
}
