package com.example.controller.graphGeneratorSettings;

import com.example.settings.AlgorithmSetting;
import com.example.settings.IntegerSettingTextField;
import javafx.scene.control.Slider;

public class GraphSettingsHelper {

    public static void bindSliderWithTextField(Slider slider, IntegerSettingTextField field) {
        field.setContainedSetting(new AlgorithmSetting<>
                ("vertices", (int) slider.getValue(), Integer.class,
                        (value) -> value >= slider.getMin() && value <= slider.getMax()));

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                field.getContainedSetting().get().setValue(newValue.intValue());
            }
        });
        field.getContainedSetting().get().getValueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                slider.setValue(newValue);
            }
        });
    }

}