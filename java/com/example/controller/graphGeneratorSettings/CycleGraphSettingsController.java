package com.example.controller.graphGeneratorSettings;

import com.example.draw.DefinedGraph;
import com.example.settings.IntegerSettingTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("cycleGraph")
@FxmlView("/view/cycleGraphSettingsView.fxml")
public class CycleGraphSettingsController implements GraphSettings {
    @FXML
    public VBox container;

    @FXML
    public IntegerSettingTextField vertices;

    @FXML
    public Slider verticesSlider;

    @FXML
    public void initialize() {
        GraphSettingsHelper.bindSliderWithTextField(verticesSlider, vertices);
    }

    @Override
    public boolean isValid() {
        return vertices.getIsValidProperty().getValue();
    }

    @Override
    public Map<String, Integer> getSettings() {
        return Map.of("numberOfVertices", vertices.getContainedSetting().get().getValue());
    }

    @Override
    public void setVisible(DefinedGraph definedGraph) {
        if (definedGraph.equals(DefinedGraph.CYCLE)) {
            container.setManaged(true);
            container.setVisible(true);
        } else {
            container.setManaged(false);
            container.setVisible(false);
        }
    }
}