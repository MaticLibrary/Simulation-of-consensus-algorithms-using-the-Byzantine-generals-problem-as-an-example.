package com.example.settings;

public class IntegerSettingTextField extends NumberSettingTextField<Integer> {
    public IntegerSettingTextField(){
        super();
    }

    @Override
    protected Integer parseInput() throws NumberFormatException {
        return Integer.parseInt(getText());
    }
}
