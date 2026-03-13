package com.example.controller.settings;

import com.example.model.MyGraph;
import com.example.settings.*;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("/view/settings/privateBftSettingsView.fxml")
public class PrivateBftSettingsController implements AlgorithmSettingsController {
    @Getter
    @FXML
    public Node parent;
    @FXML
    public Label fLabel;
    @FXML
    public Label timeoutLabel;
    @FXML
    public Label roundsLabel;
    @FXML
    private IntegerSettingTextField f;
    @FXML
    private IntegerSettingTextField timeout;
    @FXML
    private IntegerSettingTextField maxRounds;

    private SettingNodesGroup settingNodesGroup;
    @Getter
    private AlgorithmSettings algorithmSettings = new AlgorithmSettings();

    @FXML
    public void initialize() {
        setDefaultSettings();
        settingNodesGroup = new SettingNodesGroupImpl(List.of(f, timeout, maxRounds));
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
        AlgorithmSetting<Integer> fSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("f");
        AlgorithmSetting<Integer> timeoutSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("timeout");
        AlgorithmSetting<Integer> roundsSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("maxRounds");

        int maxF = graph == null ? 0 : Math.max(0, (graph.numVertices() - 1) / 2);
        fSetting.setValidateArgument((value) -> (Integer) value >= 0 && (Integer) value <= maxF);
        fSetting.setValue(Math.min(fSetting.getValue(), maxF));

        if (graph == null || graph.numVertices() == 0) {
            timeoutSetting.setValidateArgument((value) -> (Integer) value == 0);
            timeoutSetting.setValue(0);
        } else {
            timeoutSetting.setValidateArgument((value) -> (Integer) value > 0);
            timeoutSetting.setValue(Math.max(1, graph.numVertices()));
        }

        roundsSetting.setValue(Math.max(roundsSetting.getValue(), 1));
    }

    private void setDefaultSettings() {
        AlgorithmSetting<Integer> fSetting = new AlgorithmSetting<>("f", 0, Integer.class, (value) -> value >= 0);
        AlgorithmSetting<Integer> timeoutSetting = new AlgorithmSetting<>("timeout", 5, Integer.class, (value) -> value > 0);
        AlgorithmSetting<Integer> roundsSetting = new AlgorithmSetting<>("maxRounds", 5, Integer.class, (value) -> value > 0);

        f.setContainedSetting(fSetting);
        timeout.setContainedSetting(timeoutSetting);
        maxRounds.setContainedSetting(roundsSetting);

        algorithmSettings.getSettings().put("f", fSetting);
        algorithmSettings.getSettings().put("timeout", timeoutSetting);
        algorithmSettings.getSettings().put("maxRounds", roundsSetting);
    }
}
