package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/view/simulationResultView.fxml")
public class SimulationResultController {
    @FXML
    public Text simulationText;

    @FXML
    public Text consensusText;

    public void setMessage(boolean isConsensusReached) {
        if(isConsensusReached) {
            simulationText.setText("Symulacja zakończona poprawnie");
            consensusText.setText("Osiągnięto konsensus");
            consensusText.setFill(Color.rgb(20, 130, 50));
        }
        else {
            simulationText.setText("Symulacja zakończona niepowodzeniem");
            consensusText.setText("Nie osiągnięto konsensusu");
            consensusText.setFill(Color.rgb(150, 15, 15));

        }
    }
}
