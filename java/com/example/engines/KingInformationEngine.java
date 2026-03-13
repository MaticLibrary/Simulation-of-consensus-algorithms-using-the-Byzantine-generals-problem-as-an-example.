package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.util.HashMap;
import java.util.Map;

public class KingInformationEngine implements InformationEngine {
    private final InformationPrinter informationPrinter;

    public KingInformationEngine(InformationPrinter informationPrinter) {
        this.informationPrinter = informationPrinter;
    }

    @Override
    public void processReport(StepReport stepReport) {
        informationPrinter.setAlgorithmName(AlgorithmType.KING.toString());
        informationPrinter.setAlgorithmPhase(stepReport.getAlgorithmPhase());
        informationPrinter.setStepDescription(generateDescription(stepReport));
        informationPrinter.listProperties(generateProperties(stepReport));
        informationPrinter.renderView();
    }

    private Map<String, String> generateProperties(StepReport stepReport) {
        Map<String, String> properties = stepReport.getProperties();
        Map<String, String> propertiesToSend = new HashMap<>();

        propertiesToSend.put("faza", properties.getOrDefault("phase", "Nieznana"));
        propertiesToSend.put("runda", properties.getOrDefault("round", "0"));

        stepReport.getRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == VertexRole.KING)
                .findFirst()
                .ifPresentOrElse(
                        entry -> propertiesToSend.put("kr\u00f3l", entry.getKey().element().toString()),
                        () -> propertiesToSend.put("kr\u00f3l", "Brak"));

        return propertiesToSend;
    }

    private String generateDescription(StepReport stepReport) {
        switch (stepReport.getAlgorithmPhase()) {
            case SEND:
                return "Runda 1: wszyscy genera\u0142owie wymieniaj\u0105 opinie. Ka\u017cdy zlicza g\u0142osy i przyjmuje lokaln\u0105 wi\u0119kszo\u015b\u0107.";
            case CHOOSE:
                return String.format("Runda 2: kr\u00f3l rozsy\u0142a opini\u0119. Je\u015bli si\u0142a lokalnej wi\u0119kszo\u015bci by\u0142a mniejsza ni\u017c %s, genera\u0142 przyjmuje opini\u0119 kr\u00f3la jako rozstrzygaj\u0105c\u0105.",
                        stepReport.getProperties().getOrDefault("accept king opinion condition", "Brak"));
            default:
                return "Nieznana faza.";
        }
    }
}
