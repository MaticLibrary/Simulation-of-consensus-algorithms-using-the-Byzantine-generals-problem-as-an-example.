package com.example.controller.graphGeneratorSettings;

import com.example.draw.DefinedGraph;
import com.example.settings.IntegerSettingTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("fullGraph")
@FxmlView("/view/fullGraphSettingsView.fxml")
public class FullGraphSettingsController implements GraphSettings {
    @FXML
    public VBox container;

    @FXML
    private Slider verticesSlider;

    @FXML
    private IntegerSettingTextField vertices;

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
        if (definedGraph.equals(DefinedGraph.FULL)) {
            container.setVisible(true);
            container.setManaged(true);
        } else {
            container.setVisible(false);
            container.setManaged(false);
        }
    }
}