package com.example.controller;

import com.example.algorithm.AlgorithmPhase;
import com.example.engines.printer.AlgorithmProperty;
import com.example.engines.printer.InformationPrinter;
import com.example.engines.printer.PrinterElementsFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@FxmlView("/view/informationView.fxml")
public class InformationController implements InformationPrinter {
    @FXML
    private Node parent;
    @FXML
    private TextFlow properties;
    @FXML
    private Text algorithmName;
    @FXML
    private Text algorithmPhase;
    @FXML
    private TextArea stepDescription;

    @Override
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName.setText(algorithmName);
    }

    @Override
    public void setAlgorithmPhase(AlgorithmPhase algorithmPhase) {
        this.algorithmPhase.setText(algorithmPhase.toString());
    }

    @Override
    public void setStepDescription(String description) {
        this.stepDescription.setText(description);
    }

    @Override
    public void listProperties(Map<String, String> properties) {
        List<AlgorithmProperty> algorithmProperties = properties.entrySet()
                .stream()
                .map(entry -> PrinterElementsFactory.createProperty(entry.getKey(), entry.getValue()))
                .toList();
        List<Text> propertiesWithLineBreaks = algorithmProperties
                .stream()
                .flatMap(property -> Stream.of(property.getKey(), property.getValue(), new Text(System.lineSeparator())))
                .toList();
        Platform.runLater(() -> {
            this.properties.getChildren().clear();
            this.properties.getChildren().addAll(propertiesWithLineBreaks);
        });
    }

    @Override
    public void renderView() {
        parent.setVisible(true);
    }

    @Override
    public void clearView() {
        parent.setVisible(false);
    }

    @FXML
    public void initialize() {
        clearView();
    }
}
