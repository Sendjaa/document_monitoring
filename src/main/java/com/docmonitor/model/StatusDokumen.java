package com.docmonitor.model;

public enum StatusDokumen {
    AKTIF("Aktif"),
    AKAN_HABIS("Akan Habis"),
    KADALUARSA("Kadaluarsa");

    private final String label;

    StatusDokumen(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
