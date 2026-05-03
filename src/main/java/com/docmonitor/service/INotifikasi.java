package com.docmonitor.service;

import com.docmonitor.model.Dokumen;

public interface INotifikasi {
    void kirimNotifikasi(Dokumen doc);
    String formatPesan(Dokumen doc);
    void logAktivitas();
}
