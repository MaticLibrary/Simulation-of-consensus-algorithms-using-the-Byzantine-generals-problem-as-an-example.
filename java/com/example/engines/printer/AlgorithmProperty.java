package com.example.engines.printer;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import lombok.Getter;

public class AlgorithmProperty extends HBox {
    @Getter
    private final Text key;
    @Getter
    private final Text value;

    public AlgorithmProperty(String key, String value) {
        this.key = new Text(key + ": ");
        this.value = new Text(value);
        this.key.setId("key");
        this.value.setId("value");
        this.getChildren().addAll(this.key, this.value);
    }

    public void setCSS(String stylesheet) {
        this.getStylesheets().add(stylesheet);
    }
}
