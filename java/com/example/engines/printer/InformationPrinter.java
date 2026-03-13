package com.example.engines.printer;

import com.example.algorithm.AlgorithmPhase;

import java.util.Map;

public interface InformationPrinter {
    void setAlgorithmName(String algorithmName);

    void setAlgorithmPhase(AlgorithmPhase algorithmPhase);

    void setStepDescription(String description);

    void listProperties(Map<String, String> properties);

    void renderView();

    void clearView();
}
