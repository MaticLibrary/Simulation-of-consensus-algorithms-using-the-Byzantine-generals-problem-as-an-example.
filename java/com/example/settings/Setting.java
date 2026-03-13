package com.example.settings;

import javafx.beans.property.Property;

public interface Setting<T> {
    String getName();
    Boolean isProperValue(T value);
    void setValue(T value) throws IllegalArgumentException;
    Class<T> getContainedClass();
    T getValue();
    Property<T> getValueProperty();
}
