package com.example.algorithm;

import com.example.algorithm.report.StepReport;
import com.example.model.MyGraph;
import com.example.settings.AlgorithmSettings;
import javafx.beans.property.BooleanProperty;


public interface Algorithm {
    AlgorithmType getType();
    void loadEnvironment(MyGraph<Integer, Integer> graph, AlgorithmSettings settings);
    StepReport step();
    boolean isFinished();
    BooleanProperty getIsFinishedProperty();
}
