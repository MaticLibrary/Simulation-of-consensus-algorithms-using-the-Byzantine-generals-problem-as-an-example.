package com.example.settings;

import javafx.beans.property.BooleanProperty;

import java.util.Optional;

public interface SettingNode<T> {
    BooleanProperty getIsValidProperty();
    Optional<Setting<T>> getContainedSetting();
    void setContainedSetting(Setting<T> setting);
}
