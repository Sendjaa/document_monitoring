package com.docmonitor.service;

import com.docmonitor.model.Dokumen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseNotifikasi implements INotifikasi {

    private static final Logger log = LoggerFactory.getLogger(BaseNotifikasi.class);

    protected String templatePesan;
    protected String channel;

    public BaseNotifikasi(String templatePesan, String channel) {
        this.templatePesan = templatePesan;
        this.channel = channel;
    }

    @Override
    public abstract void kirimNotifikasi(Dokumen doc);

    @Override
    public String formatPesan(Dokumen doc) {
        long sisaHari = doc.getSisaHari();
        if (sisaHari < 0) {
            return String.format(templatePesan,
                doc.getNamaDokumen(),
                "telah KADALUARSA",
                doc.getTanggalBerakhir(),
                "Segera perbarui dokumen Anda!"
            );
        }
        return String.format(templatePesan,
            doc.getNamaDokumen(),
            "akan berakhir dalam " + sisaHari + " hari",
            doc.getTanggalBerakhir(),
            "Harap segera lakukan perpanjangan."
        );
    }

    @Override
    public void logAktivitas() {
        log.info("[{}] Notifikasi dikirim pada channel: {}", getClass().getSimpleName(), channel);
    }
}
