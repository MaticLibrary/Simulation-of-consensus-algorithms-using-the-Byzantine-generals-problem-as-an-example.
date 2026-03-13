package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.util.HashMap;
import java.util.Map;

public class LamportInformationEngine implements InformationEngine {
    private final InformationPrinter informationPrinter;

    public LamportInformationEngine(InformationPrinter informationPrinter) {
        this.informationPrinter = informationPrinter;
    }

    @Override
    public void processReport(StepReport stepReport) {
        informationPrinter.setAlgorithmName(AlgorithmType.LAMPORT.toString());
        informationPrinter.setAlgorithmPhase(stepReport.getAlgorithmPhase());
        informationPrinter.setStepDescription(generateDescription(stepReport));
        informationPrinter.listProperties(generateProperties(stepReport));
        informationPrinter.renderView();
    }

    private String generateDescription(StepReport stepReport) {
        switch (stepReport.getAlgorithmPhase()) {
            case SEND:
                return "OM(m) \u2013 faza wysy\u0142ania: dow\u00f3dca rozsy\u0142a opini\u0119, a porucznicy zapisuj\u0105 j\u0105 w tabeli wiedzy i przygotowuj\u0105 do dalszej propagacji.";
            case CHOOSE:
                return "Faza decyzji: po zebraniu opinii porucznicy wybieraj\u0105 wi\u0119kszo\u015b\u0107 z tabeli wiedzy i ustawiaj\u0105 j\u0105 jako decyzj\u0119.";
            default:
                return "Nieznana faza.";
        }
    }

    private Map<String, String> generateProperties(StepReport stepReport) {
        Map<String, String> properties = stepReport.getProperties();
        Map<String, String> propertiesToSend = new HashMap<>();

        propertiesToSend.put("g\u0142\u0119boko\u015b\u0107 (m)", properties.getOrDefault("depth", "N/A"));

        stepReport.getRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == VertexRole.COMMANDER)
                .findFirst()
                .ifPresent(entry -> {
                    propertiesToSend.put("dow\u00f3dca", entry.getKey().element().toString());
                    propertiesToSend.put("porucznicy", "Wierzcho\u0142ki z zielon\u0105 po\u015bwiat\u0105");
                });

        return propertiesToSend;
    }
}
