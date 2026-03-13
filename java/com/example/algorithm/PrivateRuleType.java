package com.example.algorithm;

import lombok.Getter;

public enum PrivateRuleType {
    MAJORITY("Wi\u0119kszo\u015b\u0107", (n0, n1, leader, n) -> n1 > n0),
    MAJORITY_LEADER_TIE("Wi\u0119kszo\u015b\u0107, remis \u2192 lider", (n0, n1, leader, n) -> {
        if (n1 > n0) return true;
        if (n0 > n1) return false;
        return leader != null ? leader : false;
    }),
    LEADER_IF_DEFINED_ELSE_MAJORITY("Lider je\u015bli znany, inaczej wi\u0119kszo\u015b\u0107", (n0, n1, leader, n) -> {
        if (leader != null) return leader;
        return n1 > n0;
    }),
    LEADER_ALWAYS_OR_ZERO("Lider je\u015bli znany, inaczej 0", (n0, n1, leader, n) -> leader != null ? leader : false),
    SUPERMAJORITY_2_3("Superwi\u0119kszo\u015b\u0107 2/3", (n0, n1, leader, n) -> n1 >= ceilDiv(2 * n, 3)),
    SUPERMAJORITY_3_4("Superwi\u0119kszo\u015b\u0107 3/4", (n0, n1, leader, n) -> n1 >= ceilDiv(3 * n, 4)),
    STRICT_HALF("Powy\u017cej po\u0142owy", (n0, n1, leader, n) -> n1 > n / 2),
    STRICT_HALF_BIASED_ONE("Powy\u017cej po\u0142owy dla 0, inaczej 1", (n0, n1, leader, n) -> n0 > n / 2 ? false : true),
    BIASED_TO_ZERO("Przechy\u0142 na 0 (n1 >= n0 + 2)", (n0, n1, leader, n) -> n1 >= n0 + 2),
    BIASED_TO_ONE("Przechy\u0142 na 1 (n0 >= n1 + 2)", (n0, n1, leader, n) -> n0 >= n1 + 2 ? false : true),
    PARITY_LEADER("Parzysto\u015b\u0107 g\u0142os\u00f3w \u2192 lider", (n0, n1, leader, n) -> {
        if (((n0 + n1) % 2) == 0) {
            return leader != null ? leader : (n1 > n0);
        }
        return n1 > n0;
    }),
    MINIMUM_QUORUM("Minimalny kworum \u2192 lider", (n0, n1, leader, n) -> {
        if (n0 + n1 < n / 2) {
            return leader != null ? leader : false;
        }
        return n1 > n0;
    }),
    LEADER_ON_TIE_OR_LOW("Remis lub ma\u0142o g\u0142os\u00f3w \u2192 lider", (n0, n1, leader, n) -> {
        if (n1 == n0 || (n0 + n1) < ceilDiv(6 * n, 10)) {
            return leader != null ? leader : false;
        }
        return n1 > n0;
    }),
    ONE_IF_ANY_ONE("1 je\u015bli ktokolwiek za 1", (n0, n1, leader, n) -> n1 > 0),
    ONE_IF_ALL_ONE("1 tylko gdy wszyscy za 1", (n0, n1, leader, n) -> n1 == n),
    ZERO_IF_ANY_ZERO("0 je\u015bli ktokolwiek za 0", (n0, n1, leader, n) -> n0 > 0 ? false : true),
    ZERO_IF_ALL_ZERO("0 tylko gdy wszyscy za 0", (n0, n1, leader, n) -> n0 == n ? false : true),
    WEIGHTED_LEADER_DOUBLE("Lider liczony podw\u00f3jnie", (n0, n1, leader, n) -> {
        int w0 = n0;
        int w1 = n1;
        if (leader != null) {
            if (leader) w1 += 1;
            else w0 += 1;
        }
        return w1 > w0;
    }),
    TIE_DEFAULT_ONE("Remis \u2192 lider, inaczej 1", (n0, n1, leader, n) -> {
        if (n1 > n0) return true;
        if (n0 > n1) return false;
        return leader != null ? leader : true;
    }),
    THRESHOLD_60("Pr\u00f3g 60%", (n0, n1, leader, n) -> n1 >= ceilDiv(6 * n, 10)),
    LEADER_ONLY("Tylko lider (brak \u2192 0)", (n0, n1, leader, n) -> leader != null ? leader : false),
    LEADER_ONLY_ONE("Tylko lider (brak \u2192 1)", (n0, n1, leader, n) -> leader != null ? leader : true),
    QUORUM_40("Kworum 40%", (n0, n1, leader, n) -> n1 >= ceilDiv(4 * n, 10)),
    QUORUM_70("Kworum 70%", (n0, n1, leader, n) -> n1 >= ceilDiv(7 * n, 10)),
    MAJORITY_MARGIN_ONE("Wi\u0119kszo\u015b\u0107 +1 dla 1", (n0, n1, leader, n) -> n1 >= n0 + 1),
    MAJORITY_MARGIN_TWO("Wi\u0119kszo\u015b\u0107 +2 dla 1", (n0, n1, leader, n) -> n1 >= n0 + 2),
    DEFENSIVE_ZERO("Defensywnie 0 przy ma\u0142ej pr\u00f3bie", (n0, n1, leader, n) -> {
        if (n0 + n1 < ceilDiv(n, 2)) {
            return false;
        }
        return n1 > n0;
    }),
    LEADER_ON_LOW_QUORUM("Lider przy niskim kworum (<60%)", (n0, n1, leader, n) -> {
        if (n0 + n1 < ceilDiv(6 * n, 10)) {
            return leader != null ? leader : false;
        }
        return n1 > n0;
    }),
    EVEN_TOTAL_ONE("Parzysta liczba g\u0142os\u00f3w \u2192 1", (n0, n1, leader, n) -> {
        if (((n0 + n1) % 2) == 0) {
            return true;
        }
        return n1 > n0;
    }),
    STRICT_HALF_OR_LEADER("Tylko >1/2, inaczej lider", (n0, n1, leader, n) -> {
        if (n1 > n / 2) return true;
        if (n0 > n / 2) return false;
        return leader != null ? leader : false;
    });

    @Getter
    private final String label;
    private final RuleEvaluator evaluator;

    PrivateRuleType(String label, RuleEvaluator evaluator) {
        this.label = label;
        this.evaluator = evaluator;
    }

    public boolean evaluate(int n0, int n1, Boolean leaderValue, int n) {
        int safeN = Math.max(n, 1);
        return evaluator.evaluate(n0, n1, leaderValue, safeN);
    }

    private static int ceilDiv(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    @FunctionalInterface
    private interface RuleEvaluator {
        boolean evaluate(int n0, int n1, Boolean leaderValue, int n);
    }
}
