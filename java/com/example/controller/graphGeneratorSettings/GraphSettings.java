package com.example.controller.graphGeneratorSettings;

import com.example.draw.DefinedGraph;

import java.util.Map;

public interface GraphSettings {

    boolean isValid();

    Map<String, Integer> getSettings();

    void setVisible(DefinedGraph definedGraph);
}