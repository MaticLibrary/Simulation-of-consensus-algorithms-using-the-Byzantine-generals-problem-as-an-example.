package com.example.controller.graphGeneratorSettings;

import com.example.draw.DefinedGraph;
import com.example.settings.IntegerSettingTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("treeGraph")
@FxmlView("/view/treeGraphSettingsView.fxml")
public class TreeGraphSettingsController implements GraphSettings{
    @FXML
    public VBox container;

    @FXML
    public IntegerSettingTextField children;

    @FXML
    public Slider childrenSlider;

    @FXML
    public IntegerSettingTextField height;

    @FXML
    public Slider heightSlider;

    @FXML
    public void initialize() {
        GraphSettingsHelper.bindSliderWithTextField(childrenSlider, children);
        GraphSettingsHelper.bindSliderWithTextField(heightSlider, height);
    }

    @Override
    public boolean isValid() {
        return children.getIsValidProperty().getValue() && height.getIsValidProperty().getValue();
    }

    @Override
    public Map<String, Integer> getSettings() {
        return Map.of("numberOfChildren", children.getContainedSetting().get().getValue(),
                "height", height.getContainedSetting().get().getValue());
    }

    @Override
    public void setVisible(DefinedGraph definedGraph) {
        if (definedGraph.equals(DefinedGraph.TREE)) {
            container.setManaged(true);
            container.setVisible(true);
        } else {
            container.setManaged(false);
            container.setVisible(false);
        }
    }
}