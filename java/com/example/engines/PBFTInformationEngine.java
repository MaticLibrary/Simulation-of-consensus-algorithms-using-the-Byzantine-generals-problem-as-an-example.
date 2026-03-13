package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.algorithm.VertexRole;
import com.example.algorithm.report.StepReport;
import com.example.engines.printer.InformationPrinter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class PBFTInformationEngine implements InformationEngine {
    private final InformationPrinter informationPrinter;

    public PBFTInformationEngine(InformationPrinter informationPrinter) {
        this.informationPrinter = informationPrinter;
    }

    @Override
    public void processReport(StepReport stepReport) {
        informationPrinter.setAlgorithmName(AlgorithmType.PBFT.toString());
        informationPrinter.setAlgorithmPhase(stepReport.getAlgorithmPhase());
        informationPrinter.setStepDescription(generateDescription(stepReport));
        informationPrinter.listProperties(generateProperties(stepReport));
        informationPrinter.renderView();
    }

    private Map<String, String> generateProperties(StepReport stepReport) {
        Map<String, String> properties = stepReport.getProperties();

        Map<String, String> propertiesToSend = new HashMap<>();
        propertiesToSend.put("czas", properties.getOrDefault("time", "N/A"));
        propertiesToSend.put("p (rozstrzygni\u0119cie remisu)", generateProbability(properties.get("probability")));

        stepReport.getRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == VertexRole.VOTER)
                .findFirst()
                .ifPresent(entry -> {
                    propertiesToSend.put("g\u0142osuj\u0105cy", entry.getKey().element().toString());
                    propertiesToSend.put("s\u0105siedzi", "Wierzcho\u0142ki z zielon\u0105 po\u015bwiat\u0105");
                });

        return propertiesToSend;
    }

    private String generateDescription(StepReport stepReport) {
        return switch (stepReport.getAlgorithmPhase()) {
            case SEND -> "Faza zbierania: wylosowany g\u0142osuj\u0105cy zbiera opinie od s\u0105siad\u00f3w.";
            case CHOOSE -> "Faza decyzji: g\u0142osuj\u0105cy akceptuje sp\u00f3jn\u0105 opini\u0119, a przy konflikcie rozstrzyga remis z prawdopodobie\u0144stwem p.";
            default -> "Nieznana faza.";
        };
    }

    private String generateProbability(String probabilityStr) {
        double probability = Optional.ofNullable(probabilityStr).map(Double::parseDouble).orElse(0.0);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(probability);
    }
}
