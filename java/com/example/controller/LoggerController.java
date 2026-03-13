package com.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import net.rgielen.fxweaver.core.FxmlView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@FxmlView("/view/loggerView.fxml")
public class LoggerController {
    @FXML
    public ListView<String> listView;

    private static final String JSON_FILE_PATH = "database.json";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "logger-io");
        t.setDaemon(true);
        return t;
    });

    @FXML
    public void initialize() {
    }

    public void addItem(String item) {
        Platform.runLater(() -> listView.getItems().add(item));
        IO_EXECUTOR.execute(() -> saveItemToJson(item));
    }

    private void saveItemToJson(String item) {
        //pomijanie znaków EOL
        if (item.trim().isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        JSONObject record = new JSONObject();
        record.put("timestamp", timestamp);
        record.put("item", item);

        try {
            JSONObject newJson;
            File file = new File(JSON_FILE_PATH);

            if (!file.exists() || file.length() == 0) {
                newJson = new JSONObject();
                newJson.put("simulations", new JSONArray());
            } else {
                String content = new String(Files.readAllBytes(Paths.get(JSON_FILE_PATH)));
                newJson = new JSONObject(content);
            }

            JSONArray simulations = newJson.getJSONArray("simulations");
            JSONObject currentSimulation = simulations.length() > 0 ? simulations.getJSONObject(simulations.length() - 1) : null;
            boolean isNewSimulation = item.contains("[Początek]");

            if (isNewSimulation) {
                currentSimulation = new JSONObject();
                currentSimulation.put("logs", new JSONArray());
                simulations.put(currentSimulation);
            } else if (currentSimulation != null) {
                JSONArray logs = currentSimulation.getJSONArray("logs");

                // Dodanie logu tylko jeśli nie jest to początek symulacji lub jeśli jest pierwszy [Zdarzenie] po [Początek]
                if (logs.length() == 0 && !item.contains("[Zdarzenie]")) {
                    // Czekamy na pierwszy [Zdarzenie] przed ustawieniem startTimestamp
                } else {
                    // Ustawienie startTimestamp na pierwszy [Zdarzenie] po [Początek]
                    if (!currentSimulation.has("startTimestamp") && item.contains("[Zdarzenie]")) {
                        currentSimulation.put("startTimestamp", timestamp);
                    }
                    logs.put(record);
                }

            }
            if (item.contains("[Koniec]") && currentSimulation != null) {
                currentSimulation.put("endTimestamp", timestamp);
            }

            try (FileWriter fileWriter = new FileWriter(JSON_FILE_PATH)) {
                fileWriter.write(newJson.toString(4));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
