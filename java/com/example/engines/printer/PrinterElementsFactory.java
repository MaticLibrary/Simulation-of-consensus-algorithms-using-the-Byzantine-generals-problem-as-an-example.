package com.example.engines.printer;

public class PrinterElementsFactory {
    public static AlgorithmProperty createProperty(String key, String value) {
        return new AlgorithmProperty(key, value);
    }
}
