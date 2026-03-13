package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.util.HashMap;
import java.util.Map;

public class PrivateRulesBftInformationEngine implements InformationEngine {
    private final InformationPrinter informationPrinter;

    public PrivateRulesBftInformationEngine(InformationPrinter informationPrinter) {
        this.informationPrinter = informationPrinter;
    }

    @Override
    public void processReport(StepReport stepReport) {
        informationPrinter.setAlgorithmName(AlgorithmType.PRIVATE_BFT.toString());
        informationPrinter.setAlgorithmPhase(stepReport.getAlgorithmPhase());
        informationPrinter.setStepDescription(generateDescription(stepReport));
        informationPrinter.listProperties(generateProperties(stepReport));
        informationPrinter.renderView();
    }

    private Map<String, String> generateProperties(StepReport stepReport) {
        Map<String, String> properties = stepReport.getProperties();
        Map<String, String> result = new HashMap<>();

        result.put("runda", properties.getOrDefault("runda", "N/A"));
        result.put("lider", properties.getOrDefault("lider", "N/A"));
        result.put("f", properties.getOrDefault("f", "N/A"));
        result.put("timeout", properties.getOrDefault("timeout", "N/A"));
        result.put("alarmy", properties.getOrDefault("alarmy", "0"));
        result.put("regu\u0142y", properties.getOrDefault("regu\u0142y", "N/A"));

        stepReport.getRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == VertexRole.COMMANDER)
                .findFirst()
                .ifPresent(entry -> result.put("lider (wierzcho\u0142ek)", entry.getKey().element().toString()));

        return result;
    }

    private String generateDescription(StepReport stepReport) {
        return switch (stepReport.getAlgorithmPhase()) {
            case SEND -> "PROPOSAL/ECHO: lider rozsy\u0142a propozycj\u0119, a w\u0119z\u0142y propaguj\u0105 echa z list\u0105 podpis\u00f3w.";
            case CHOOSE -> "Decyzja: w\u0119z\u0142y licz\u0105 unikalne podpisy (n0/n1), wyznaczaj\u0105 decyzj\u0119 globaln\u0105 i por\u00f3wnuj\u0105 j\u0105 z regu\u0142\u0105 prywatn\u0105. Rozbie\u017cno\u015b\u0107 \u2192 ALARM.";
            default -> "Nieznana faza.";
        };
    }
}
