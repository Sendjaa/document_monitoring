package com.docmonitor.model;

public enum TipeKategori {
    INDIVIDU("Individu"),
    BERSAMA("Bersama");

    private final String displayName;

    TipeKategori(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
