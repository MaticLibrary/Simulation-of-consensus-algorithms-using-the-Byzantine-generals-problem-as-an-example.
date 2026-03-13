package com.example.controller;

import com.example.draw.OpinionGenerator;
import com.example.draw.TraitorsGenerator;
import com.example.model.MyGraph;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/view/generateVerticesView.fxml")
public class GenerateVerticesController {
    @FXML
    private Slider distributionSlider;

    @Autowired
    private TraitorsGenerator traitorsGenerator;

    @Autowired
    private OpinionGenerator opinionGenerator;

    public void generateTraitors(MyGraph<Integer, Integer> graph) {
        traitorsGenerator.generateTraitors(graph, distributionSlider.getValue()/100);
    }

    public void generateAttackers(MyGraph<Integer, Integer> graph) {
        opinionGenerator.generateAttackers(graph, distributionSlider.getValue()/100);
    }
}
