package com.example.controller.settings;

public class TraitorSettings {
    public static boolean isTraitorsAlwaysLie() {
        return traitorsAlwaysLie;
    }

    public static void setTraitorsAlwaysLie(boolean traitorsAlwaysLie) {
        TraitorSettings.traitorsAlwaysLie = traitorsAlwaysLie;
    }

    private static boolean traitorsAlwaysLie = false;
}
