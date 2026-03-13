package com.example.engines;

import com.example.algorithm.AlgorithmType;
import com.example.engines.printer.InformationPrinter;

public class InformationEngineFactory {
    public static InformationEngine createForAlgorithm(AlgorithmType algorithmType, InformationPrinter informationPrinter) {
        switch (algorithmType) {
            case LAMPORT -> {
                return new LamportInformationEngine(informationPrinter);
            }
            case KING -> {
                return new KingInformationEngine(informationPrinter);
            }
            case PBFT -> {
                return new PBFTInformationEngine(informationPrinter);
            }
            case PRIVATE_BFT -> {
                return new PrivateRulesBftInformationEngine(informationPrinter);
            }
        }
        throw new IllegalArgumentException("There is no InformationEngine for " + algorithmType);
    }
}
