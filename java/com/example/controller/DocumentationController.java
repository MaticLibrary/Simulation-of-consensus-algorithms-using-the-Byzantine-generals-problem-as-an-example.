package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.example.algorithm.AlgorithmType;

@Component
@FxmlView("/view/documentationView.fxml")
public class DocumentationController {

    private Stage documentationStage;

    @FXML
    ListView<Label> articleList;

    @FXML
    Pane page;

    private static final Map<Integer, String> pageLocations = new HashMap<>();

    static {
        pageLocations.put(0, "/view/documentationPages/userGuide.fxml");
        pageLocations.put(1, "/view/documentationPages/helpLamport.fxml");
        pageLocations.put(2, "/view/documentationPages/helpKing.fxml");
        pageLocations.put(3, "/view/documentationPages/helpPBFT.fxml");
        pageLocations.put(4, "/view/documentationPages/helpPrivateBft.fxml");
    }

    private void setScene(int index, Pane page) {
        String location = pageLocations.get(index);
        if (location == null) {
            throw new IllegalStateException("Unexpected value: " + index);
        }

        try {
            Node scene = FXMLLoader.load(getClass().getResource(location));
            if (scene instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                AnchorPane.setTopAnchor(region, 0.0);
                AnchorPane.setBottomAnchor(region, 0.0);
                AnchorPane.setLeftAnchor(region, 0.0);
                AnchorPane.setRightAnchor(region, 0.0);
            }
            page.getChildren().setAll(scene);
        } catch (IOException e) {
            // Consider logging the error and showing a user-friendly message
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        articleList.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Label item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (!item.getStyleClass().contains("docs-item")) {
                        item.getStyleClass().add("docs-item");
                    }
                    setText(null);
                    setGraphic(item);
                }
            }
        });
        articleList.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> setScene(t1.intValue(), page)
        );
    }

    public void openDocumentation(int index) throws IOException {
        if (documentationStage == null) {
            documentationStage = createDocumentationStage();
        }
        showDocumentationStage(index);
    }

    public void openDocumentationForAlgorithm(AlgorithmType algorithmType) throws IOException {
        int index = switch (algorithmType) {
            case LAMPORT -> 1;
            case KING -> 2;
            case PBFT -> 3;
            case PRIVATE_BFT -> 4;
        };
        openDocumentation(index);
    }

    private Stage createDocumentationStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/documentationView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Dokumentacja");
        stage.setScene(scene);
        return stage;
    }

    private void showDocumentationStage(int index) {
        if (!documentationStage.isShowing()) {
            documentationStage.show();
        }
        documentationStage.toFront();
        if (documentationStage.isIconified()) {
            documentationStage.setIconified(false);
        }

        ListView<Label> listView = (ListView<Label>) documentationStage.getScene().lookup("#articleList");
        listView.getSelectionModel().select(index);
    }
}
