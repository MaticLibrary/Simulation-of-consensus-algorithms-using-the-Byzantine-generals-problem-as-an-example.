package com.example.controller;

import com.example.util.StatisticsConverter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

@Component
@FxmlView("/view/statisticsView.fxml")
public class StatisticsController {
    @FXML
    private Button exportButton;
    @FXML
    private LineChart<Number, Number> opinionChart;
    @FXML
    private Slider timelineSlider;
    @FXML
    private Label timelineValueLabel;
    @FXML
    private Label lastOperationLabel;
    @FXML
    private Label supportCountLabel;
    @FXML
    private Label opposeCountLabel;

    private int nextX = 0;
    private final XYChart.Series<Number, Number> supporting = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> notSupporting = new XYChart.Series<>();

    @FXML
    public void initialize() {
        configureExportButton();
        configureChartAxes();
        configureTimeline();
        supporting.setName("Za atakiem [%]");
        notSupporting.setName("Za odwrotem [%]");
        opinionChart.getData().add(supporting);
        opinionChart.getData().add(notSupporting);
        opinionChart.setCreateSymbols(false);
    }

    private void configureExportButton() {
        exportButton.setOnAction(event -> {
            try {
                exportStats();
            } catch (Exception e) {
                // Log error or show dialog to the user
                e.printStackTrace();
            }
        });
    }

    private void configureChartAxes() {
        NumberAxis yAxis = (NumberAxis) opinionChart.getYAxis();
        NumberAxis xAxis = (NumberAxis) opinionChart.getXAxis();

        yAxis.setLabel("Generałowie za atakiem [%]");
        xAxis.setLabel("Krok");

        yAxis.setLowerBound(0);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(10);
        yAxis.setUpperBound(100);

        yAxis.setMinorTickLength(0);
        xAxis.setMinorTickLength(0);

        yAxis.setTickUnit(10);

        yAxis.setAutoRanging(false);
        xAxis.setAutoRanging(true);

        StringConverter<Number> onlyIntegers = new StringConverter<>() {
            @Override
            public String toString(Number number) {
                return String.valueOf(number.intValue());
            }

            @Override
            public Number fromString(String string) {
                return Double.parseDouble(string);
            }
        };

        yAxis.setTickLabelFormatter(onlyIntegers);
        xAxis.setTickLabelFormatter(onlyIntegers);
    }

    private void configureTimeline() {
        if (timelineSlider == null) {
            return;
        }
        timelineSlider.setMin(0);
        timelineSlider.setMax(1);
        timelineSlider.setValue(0);
        timelineSlider.setBlockIncrement(1);
        timelineSlider.setDisable(true);
        timelineSlider.setFocusTraversable(false);
        timelineSlider.setMouseTransparent(true);
        if (timelineValueLabel != null) {
            timelineValueLabel.setText("Krok: 0");
        }
        if (lastOperationLabel != null) {
            lastOperationLabel.setText("-");
        }
    }

    private void exportStats() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz statystyki");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file != null) {
            try {
                StatisticsConverter.exportStats(file, supporting, notSupporting);
            } catch (IOException e) {
                // Log error or show dialog to the user
                e.printStackTrace();
            }
        }
    }

    public void addStats(int numSupporting, int numNotSupporting) {
        Platform.runLater(() -> {
            int total = numSupporting + numNotSupporting;
            double percentage = total == 0 ? 0.0 : (double) numSupporting / total * 100;
            double percentageOppose = total == 0 ? 0.0 : (double) numNotSupporting / total * 100;
            supporting.getData().add(new XYChart.Data<>(nextX, percentage));
            notSupporting.getData().add(new XYChart.Data<>(nextX, percentageOppose));
            nextX++;
            updateTimeline(nextX - 1);
            updateCounts(numSupporting, numNotSupporting, percentage, percentageOppose);
        });
    }

    public void clear() {
        Platform.runLater(() -> {
            opinionChart.getData().clear();
            supporting.getData().clear();
            notSupporting.getData().clear();
            nextX = 0;
            opinionChart.getData().add(supporting);
            opinionChart.getData().add(notSupporting);
            updateTimeline(0);
            if (lastOperationLabel != null) {
                lastOperationLabel.setText("-");
            }
            updateCounts(0, 0, 0.0, 0.0);
        });
    }

    public void setLastOperation(String description) {
        if (lastOperationLabel == null) {
            return;
        }
        Platform.runLater(() -> lastOperationLabel.setText(description == null || description.isBlank() ? "-" : description));
    }

    private void updateTimeline(int step) {
        if (timelineSlider == null) {
            return;
        }
        timelineSlider.setMax(Math.max(timelineSlider.getMax(), step));
        timelineSlider.setValue(step);
        if (timelineValueLabel != null) {
            timelineValueLabel.setText("Krok: " + step);
        }
    }

    private void updateCounts(int numSupporting, int numNotSupporting, double supportPct, double opposePct) {
        if (supportCountLabel != null) {
            supportCountLabel.setText(numSupporting + " (" + Math.round(supportPct) + "%)");
        }
        if (opposeCountLabel != null) {
            opposeCountLabel.setText(numNotSupporting + " (" + Math.round(opposePct) + "%)");
        }
    }
}
