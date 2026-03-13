package com.example.settings;

import javafx.beans.property.BooleanProperty;

import java.util.List;

public interface SettingNodesGroup {
    BooleanProperty getAreAllValidProperty();

    List<SettingNode<?>> getAllNodes();
}
