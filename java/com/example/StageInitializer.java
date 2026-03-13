package com.example;


import com.example.controller.AppController;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<ByzantineGeneralsApplicationJavaFX.StageReadyEvent> {

    private final FxWeaver fxWeaver;

    @Autowired
    public StageInitializer(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(ByzantineGeneralsApplicationJavaFX.StageReadyEvent event) {
        Stage stage = event.getStage();
        AppController appController = fxWeaver.loadController(AppController.class);
        configureStage(stage, appController.getRoot());
        stage.show();
        appController.initGraph();
    }

    private void configureStage(Stage primaryStage, BorderPane root) {
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Generałowie Bizantyjscy");
    }
}
