package com.example.algorithm;

public enum ProbabilityType {
    AUTO,
    ;

    @Override
    public String toString() {
        return switch (this) {
            case AUTO -> "Auto";
        };
    }
}
