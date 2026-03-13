package com.example.controller.settings;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.model.MyGraph;
import com.example.settings.*;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("/view/settings/lamportSettingsView.fxml")
public class LamportSettingsController implements AlgorithmSettingsController {
    @Getter
    @FXML
    public Node parent;
    @FXML
    public Label depthLabel;
    @FXML
    private IntegerSettingTextField depth;
    private SettingNodesGroup settingNodesGroup;
    @Getter
    private AlgorithmSettings algorithmSettings = new AlgorithmSettings();

    @FXML
    public void initialize() {
        setDefaultSettings();
        settingNodesGroup = new SettingNodesGroupImpl(Collections.singletonList(depth));
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
        AlgorithmSetting<Integer> depthSetting = (AlgorithmSetting<Integer>) algorithmSettings.getSettings().get("depth");
        if (graph == null || graph.numVertices() == 0) {
            depthSetting.setValidateArgument((value) -> (Integer) value == 0);
            depthSetting.setValue(0);
            return;
        }
        Vertex<Integer> commander = graph.vertices().stream().toList().get(0);
        int maxDepth = graph.getLongestPathFor(commander);
        depthSetting.setValidateArgument((value) -> (Integer) value >= 0 && (Integer) value <= maxDepth);
        depthSetting.setValue(Math.min(graph.getTraitorsCount(), maxDepth));
    }

    private void setDefaultSettings() {
        AlgorithmSetting<Integer> depthSetting = new AlgorithmSetting<>("depth", 1, Integer.class, (value) -> value >= 0);
        algorithmSettings.getSettings().put("depth", depthSetting);
        depth.setContainedSetting(depthSetting);
    }
}
