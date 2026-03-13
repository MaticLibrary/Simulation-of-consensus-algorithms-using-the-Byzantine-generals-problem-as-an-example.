package com.example.algorithm;

import com.example.model.MyGraph;
import com.example.settings.AlgorithmSetting;
import com.example.settings.AlgorithmSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateRulesBftAlgorithmTest {

    @Test
    void privateBftFinishesWithinMaxRounds() {
        MyGraph<Integer, Integer> graph = new MyGraph<>();
        for (int i = 0; i < 4; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                graph.insertEdge(i, j, 1);
            }
        }

        AlgorithmSettings settings = new AlgorithmSettings();
        AlgorithmSetting<Integer> fSetting = new AlgorithmSetting<>("f", 1, Integer.class, value -> value >= 0);
        AlgorithmSetting<Integer> timeoutSetting = new AlgorithmSetting<>("timeout", 4, Integer.class, value -> value > 0);
        AlgorithmSetting<Integer> roundsSetting = new AlgorithmSetting<>("maxRounds", 2, Integer.class, value -> value > 0);
        settings.getSettings().put("f", fSetting);
        settings.getSettings().put("timeout", timeoutSetting);
        settings.getSettings().put("maxRounds", roundsSetting);

        PrivateRulesBftAlgorithm algorithm = new PrivateRulesBftAlgorithm();
        algorithm.loadEnvironment(graph, settings);

        int steps = 0;
        while (!algorithm.isFinished() && steps < 10) {
            assertNotNull(algorithm.step());
            steps++;
        }

        assertTrue(algorithm.isFinished());
        assertTrue(steps <= 4);
    }
}
