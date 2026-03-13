package com.example.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import lombok.Getter;

import java.util.Optional;

public abstract class NumberSettingTextField<T extends Number> extends TextField implements SettingNode<T>{
    private Setting<T> containedSetting;
    @Getter
    private BooleanProperty isValidProperty = new SimpleBooleanProperty(true);

    public NumberSettingTextField(){
        isValidProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue){
                setInputStyle(newValue ? InputStyle.VALID : InputStyle.INVALID);
            }
        });

        textProperty().addListener((observable, oldValue, newValue) -> {
            validateInput();
        });
    }

    @Override
    public Optional<Setting<T>> getContainedSetting() {
        return Optional.ofNullable(containedSetting);
    }

    @Override
    public void setContainedSetting(Setting<T> setting) {
        containedSetting = setting;
        setText(containedSetting.getValue().toString());
        containedSetting.getValueProperty().addListener((observable, oldValue, newValue) -> {
            setText(containedSetting.getValue().toString());
        });
    }

    private void validateInput(){
        if (containedSetting != null){
            T parsedInput;
            try{
                parsedInput = parseInput();
            }catch (NumberFormatException e){
                isValidProperty.setValue(false);
                return;
            }
            if (containedSetting.isProperValue(parsedInput)) {
                containedSetting.setValue(parsedInput);
                isValidProperty.setValue(true);
            }
            else {
                isValidProperty.setValue(false);
            }
        }
    }

    private void setInputStyle(InputStyle style){
        switch (style){
            case VALID -> setStyle("");
            case INVALID -> setStyle("-fx-text-box-border: red ;-fx-focus-color: red ;");
        }
    }

    protected abstract T parseInput() throws NumberFormatException;

    private enum InputStyle{
        VALID,
        INVALID
    }
}
