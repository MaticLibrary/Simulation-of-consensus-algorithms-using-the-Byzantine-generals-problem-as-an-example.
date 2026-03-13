package com.example.algorithm;

import com.example.util.NotImplementedException;

public enum AlgorithmType {
    LAMPORT,
    KING,
    PBFT,
    PRIVATE_BFT;

    @Override
    public String toString() {
        return switch (this) {
            case LAMPORT -> "Lamport";
            case KING -> "Kr\u00f3l";
            case PBFT -> "PBFT";
            case PRIVATE_BFT -> "BFT z regu\u0142ami prywatnymi";
        };
    }

    public Algorithm getAlgorithm() {
        switch (this) {
            case LAMPORT -> {return new LamportIterAlgorithm();}
            case KING -> {return new KingAlgorithm();}
            case PBFT -> {return new PBFTModel();}
            case PRIVATE_BFT -> {return new PrivateRulesBftAlgorithm();}
            default -> throw new NotImplementedException(this.toString() + " algorithm not implemented");
        }
    }
}
