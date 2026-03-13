package com.example.controller.graphGeneratorSettings;

import com.example.draw.DefinedGraph;
import com.example.settings.IntegerSettingTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("bipartiteGraph")
@FxmlView("/view/bipartiteGraphSettingsView.fxml")
public class BipartiteGraphSettingsController implements GraphSettings{
    @FXML
    public VBox container;

    @FXML
    public IntegerSettingTextField firstVertices;

    @FXML
    public Slider firstVerticesSlider;

    @FXML
    public IntegerSettingTextField secondVertices;

    @FXML
    public Slider secondVerticesSlider;

    @FXML
    public void initialize() {
        GraphSettingsHelper.bindSliderWithTextField(firstVerticesSlider, firstVertices);
        GraphSettingsHelper.bindSliderWithTextField(secondVerticesSlider, secondVertices);
    }

    @Override
    public boolean isValid() {
        return firstVertices.getIsValidProperty().getValue() && secondVertices.getIsValidProperty().getValue();
    }

    @Override
    public Map<String, Integer> getSettings() {
        return Map.of("numberOfVerticesInFirstSet", firstVertices.getContainedSetting().get().getValue(),
                "numberOfVerticesInSecondSet", secondVertices.getContainedSetting().get().getValue());
    }

    @Override
    public void setVisible(DefinedGraph definedGraph) {
        if (definedGraph.equals(DefinedGraph.BIPARTITE)) {
            container.setManaged(true);
            container.setVisible(true);
        } else {
            container.setManaged(false);
            container.setVisible(false);
        }
    }
}