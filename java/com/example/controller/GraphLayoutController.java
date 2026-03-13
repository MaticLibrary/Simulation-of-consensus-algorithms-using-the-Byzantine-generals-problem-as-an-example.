package com.example.controller;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.example.util.ContentZoomAndMoveHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import org.springframework.stereotype.Component;

@Component
public class GraphLayoutController {
    @FXML
    private CheckBox automaticLayout;
    @FXML
    private Slider zoom;
    @FXML
    private Button resetLayout;

    private ContentZoomAndMoveHelper contentZoomAndMoveHelper;

    public void setLayout(SmartGraphPanel<?, ?> graphView, Pane container) {
        resetAutomaticLayoutCheckbox();
        resetZoomSlider();
        bindAutomaticLayoutCheckbox(graphView);
        contentZoomAndMoveHelper = new ContentZoomAndMoveHelper(graphView, container);
        bindZoomSlider(contentZoomAndMoveHelper);
    }

    private void resetAutomaticLayoutCheckbox() {
        automaticLayout.selectedProperty().unbind();
        automaticLayout.selectedProperty().setValue(false);
    }

    private void resetZoomSlider() {
        zoom.valueProperty().unbind();
        zoom.setValue(0.0);
    }

    private void bindAutomaticLayoutCheckbox(SmartGraphPanel<?, ?> graphView) {
        automaticLayout.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty());
    }

    private void bindZoomSlider(ContentZoomAndMoveHelper contentZoomAndMoveHelper) {
        zoom.valueProperty().bind(contentZoomAndMoveHelper.scaleFactorProperty());
    }

    public void reset() {
        if (contentZoomAndMoveHelper != null) {
            contentZoomAndMoveHelper.resetLayout();
        }
    }
}
