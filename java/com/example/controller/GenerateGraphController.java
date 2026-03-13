package com.example.controller;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.example.controller.graphGeneratorSettings.*;
import com.example.draw.DefinedGraph;
import com.example.draw.GraphGenerator;
import com.example.model.MyGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"fullGraph", "cycleGraph", "treeGraph", "planarGraph", "bipartiteGraph"})
@FxmlView("/view/generateGraphView.fxml")
public class GenerateGraphController {
    @FXML
    private ComboBox<DefinedGraph> graphBox;

    @FXML
    private FullGraphSettingsController fullGraphSettingsController;

    @FXML
    private CycleGraphSettingsController cycleGraphSettingsController;

    @FXML
    private TreeGraphSettingsController treeGraphSettingsController;

    @FXML
    private PlanarGraphSettingsController planarGraphSettingsController;

    @FXML
    private BipartiteGraphSettingsController bipartiteGraphSettingsController;

    @Autowired
    private GraphGenerator graphGenerator;

    @Setter
    private DefinedGraph selectedDefinedGraph;

    @Setter
    private GraphSettings selectedGraphSettings;

    public GenerateGraphController() { }

    public void generateGraph(GraphController graphController) {
        if (selectedGraphSettings.isValid()) {
            Graph<Integer, Integer> generatedGraph = graphGenerator.generateGraph(selectedDefinedGraph, selectedGraphSettings.getSettings());
            graphController.setModelGraph((MyGraph<Integer, Integer>) generatedGraph);
        }
    }

    @FXML
    public void initialize() {
        graphBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DefinedGraph definedGraph, boolean empty) {
                super.updateItem(definedGraph, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(definedGraph.toString());
                }
            }
        });

        graphBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        setGraphSettings(newValue);
                    }
                }));

        ObservableList<DefinedGraph> definedGraphs = FXCollections.observableArrayList(DefinedGraph.FULL,
                DefinedGraph.CYCLE, DefinedGraph.BIPARTITE, DefinedGraph.TREE, DefinedGraph.PLANAR);
        graphBox.setItems(definedGraphs);
        graphBox.getSelectionModel().select(0);
    }

    private void setGraphSettings(DefinedGraph definedGraph) {
        setSelectedDefinedGraph(definedGraph);

        selectedGraphSettings = switch (selectedDefinedGraph) {
            case FULL -> fullGraphSettingsController;
            case TREE -> treeGraphSettingsController;
            case CYCLE -> cycleGraphSettingsController;
            case PLANAR -> planarGraphSettingsController;
            case BIPARTITE -> bipartiteGraphSettingsController;
        };

        fullGraphSettingsController.setVisible(definedGraph);
        treeGraphSettingsController.setVisible(definedGraph);
        cycleGraphSettingsController.setVisible(definedGraph);
        planarGraphSettingsController.setVisible(definedGraph);
        bipartiteGraphSettingsController.setVisible(definedGraph);
    }
}
