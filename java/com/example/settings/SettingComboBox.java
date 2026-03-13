package com.example.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ComboBox;
import lombok.Getter;

import java.util.Optional;

public class SettingComboBox<T> extends ComboBox<T> implements SettingNode<T> {
    private Setting<T> containedSetting;
    @Getter
    private BooleanProperty isValidProperty = new SimpleBooleanProperty(true);

    public SettingComboBox() {
        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            validateInput(newValue);
        });
    }

    @Override
    public Optional<Setting<T>> getContainedSetting() {
        return Optional.ofNullable(containedSetting);
    }

    @Override
    public void setContainedSetting(Setting<T> setting) {
        containedSetting = setting;
    }

    private void validateInput(T input) {
        if (containedSetting != null) {
            if (containedSetting.isProperValue(input)) {
                containedSetting.setValue(input);
                isValidProperty.setValue(true);
            } else {
                isValidProperty.setValue(false);
            }
        }
    }
}
