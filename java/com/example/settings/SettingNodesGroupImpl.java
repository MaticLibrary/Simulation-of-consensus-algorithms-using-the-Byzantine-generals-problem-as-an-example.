package com.example.settings;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import lombok.Getter;

import java.util.List;

public class SettingNodesGroupImpl implements SettingNodesGroup {
    private final List<SettingNode<?>> settings;
    @Getter
    private BooleanProperty areAllValidProperty = new SimpleBooleanProperty(true);

    public SettingNodesGroupImpl(List<SettingNode<?>> settings) {
        this.settings = settings;
        if (settings == null) {
            throw new IllegalArgumentException("List of settings cannot be null");
        }
        areAllValidProperty.bind(createObservableSettingsProperty());
    }

    private ObservableValue<Boolean> createObservableSettingsProperty() {
        Observable[] dependencies = settings
                .stream()
                .map(SettingNode::getIsValidProperty)
                .toArray(Observable[]::new);
        return Bindings.createBooleanBinding(() -> settings
                .stream()
                .map(SettingNode::getIsValidProperty)
                .allMatch(BooleanProperty::get), dependencies);
    }

    @Override
    public List<SettingNode<?>> getAllNodes() {
        return settings;
    }
}
