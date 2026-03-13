package com.example.controller.graphGeneratorSettings;

import com.example.draw.DefinedGraph;
import com.example.settings.IntegerSettingTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("planarGraph")
@FxmlView("/view/planarGraphSettingsView.fxml")
public class PlanarGraphSettingsController implements GraphSettings {
    @FXML
    public VBox container;

    @FXML
    public IntegerSettingTextField width;

    @FXML
    public Slider widthSlider;

    @FXML
    public IntegerSettingTextField height;

    @FXML
    public Slider heightSlider;

    @FXML
    public void initialize() {
        GraphSettingsHelper.bindSliderWithTextField(widthSlider, width);
        GraphSettingsHelper.bindSliderWithTextField(heightSlider, height);
    }

    @Override
    public boolean isValid() {
        return width.getIsValidProperty().getValue() && height.getIsValidProperty().getValue();
    }

    @Override
    public Map<String, Integer> getSettings() {
        return Map.of("width", width.getContainedSetting().get().getValue(),
                "height", height.getContainedSetting().get().getValue());
    }

    @Override
    public void setVisible(DefinedGraph definedGraph) {
        if (definedGraph.equals(DefinedGraph.PLANAR)) {
            container.setManaged(true);
            container.setVisible(true);
        } else {
            container.setManaged(false);
            container.setVisible(false);
        }
    }
}