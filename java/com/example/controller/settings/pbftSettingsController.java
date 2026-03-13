package com.example.controller.settings;

import com.example.algorithm.ProbabilityType;
import com.example.model.MyGraph;
import com.example.settings.*;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("/view/settings/pbftSettingsView.fxml")
public class pbftSettingsController implements AlgorithmSettingsController {
    @Getter
    @FXML
    public Node parent;
    @FXML
    public Label backupLabel;
    @FXML
    public Label timeoutLabel;
    @FXML
    public Label setupLabel;
    @FXML
    private IntegerSettingTextField q;
    @FXML
    private IntegerSettingTextField time;
    @FXML
    private SettingComboBox<ProbabilityType> setupBox;
    private SettingNodesGroup settingNodesGroup;
    @Getter
    private AlgorithmSettings algorithmSettings = new AlgorithmSettings();

    @FXML
    public void initialize() {
        setDefaultSettings();
        initializeProbabilityBox();
        settingNodesGroup = new SettingNodesGroupImpl(List.of(q, time, setupBox));
    }

    @Override
    public BooleanProperty getAreSettingsValidProperty() {
        return settingNodesGroup.getAreAllValidProperty();
    }

    @Override
    public List<Node> getAllNodes() {
        return settingNodesGroup.getAllNodes().stream().map(settingNode -> (Node) settingNode).collect(Collectors.toList());
    }

    @Override
    public void adjustSettingsConditions(MyGraph<Integer, Integer> graph) {
        AlgorithmSetting<Integer> qSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("q");
        AlgorithmSetting<Integer> timeSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("time");

        if (graph == null || graph.numVertices() == 0) {
            qSetting.setValidateArgument((value) -> (Integer) value == 0);
            qSetting.setValue(0);
            timeSetting.setValidateArgument((value) -> (Integer) value == 0);
            timeSetting.setValue(0);
            return;
        }

        int minDegree = graph.getMinDegree();
        qSetting.setValidateArgument((value) -> (Integer) value >= 0 && (Integer) value <= minDegree);
        qSetting.setValue(minDegree);
        timeSetting.setValidateArgument((value) -> (Integer) value > 0);
        timeSetting.setValue(Math.max(1, graph.numVertices()));
    }

    private void setDefaultSettings() {
        AlgorithmSetting<Integer> backupSetting = new AlgorithmSetting<>("q", 1, Integer.class, (value) -> value > 0);
        AlgorithmSetting<Integer> timeSetting = new AlgorithmSetting<>("time", 1, Integer.class, (value) -> value > 0);
        AlgorithmSetting<ProbabilityType> probabilitySetting = new AlgorithmSetting<>("probability", ProbabilityType.AUTO, ProbabilityType.class, (value) -> true);
        q.setContainedSetting(backupSetting);
        time.setContainedSetting(timeSetting);
        setupBox.setContainedSetting(probabilitySetting);
        algorithmSettings.getSettings().put("q", backupSetting);
        algorithmSettings.getSettings().put("time", timeSetting);
        algorithmSettings.getSettings().put("probability", probabilitySetting);
    }

    private void initializeProbabilityBox() {
        setupBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProbabilityType probabilityType, boolean empty) {
                super.updateItem(probabilityType, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(probabilityType.toString());
                }
            }
        });

        setupBox.setItems(FXCollections.observableArrayList(List.of(ProbabilityType.AUTO)));
        setupBox.getSelectionModel().select(0);
    }
}
