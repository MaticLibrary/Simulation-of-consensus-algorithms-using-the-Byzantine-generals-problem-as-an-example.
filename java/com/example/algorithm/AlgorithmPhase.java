package com.example.algorithm;

public enum AlgorithmPhase {
    SEND,
    CHOOSE;

    @Override
    public String toString() {
        return switch (this) {
            case SEND -> "Wysyłanie";
            case CHOOSE -> "Decyzja";
        };
    }
}
