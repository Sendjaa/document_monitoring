package com.docmonitor.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sistem_pengingat")
public class SistemPengingat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderId;

    @Column(nullable = false)
    private Integer intervalHari = 30;

    private LocalDate lastSent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dokumen_id", nullable = false)
    private Dokumen dokumen;

    // Constructors
    public SistemPengingat() {}

    public SistemPengingat(Integer intervalHari, LocalDate lastSent, Dokumen dokumen) {
        this.intervalHari = intervalHari;
        this.lastSent = lastSent;
        this.dokumen = dokumen;
    }

    public static SistemPengingatBuilder builder() {
        return new SistemPengingatBuilder();
    }

    // Getters and Setters
    public Long getReminderId() { return reminderId; }
    public void setReminderId(Long reminderId) { this.reminderId = reminderId; }

    public Integer getIntervalHari() { return intervalHari; }
    public void setIntervalHari(Integer intervalHari) { this.intervalHari = intervalHari; }

    public LocalDate getLastSent() { return lastSent; }
    public void setLastSent(LocalDate lastSent) { this.lastSent = lastSent; }

    public Dokumen getDokumen() { return dokumen; }
    public void setDokumen(Dokumen dokumen) { this.dokumen = dokumen; }

    // Builder pattern implementation
    public static class SistemPengingatBuilder {
        private Integer intervalHari = 30;
        private LocalDate lastSent;
        private Dokumen dokumen;

        public SistemPengingatBuilder intervalHari(Integer intervalHari) {
            this.intervalHari = intervalHari;
            return this;
        }

        public SistemPengingatBuilder lastSent(LocalDate lastSent) {
            this.lastSent = lastSent;
            return this;
        }

        public SistemPengingatBuilder dokumen(Dokumen dokumen) {
            this.dokumen = dokumen;
            return this;
        }

        public SistemPengingat build() {
            return new SistemPengingat(intervalHari, lastSent, dokumen);
        }
    }

    // =============================================
    // Business methods
    // =============================================

    /**
     * Cek apakah dokumen akan jatuh tempo dalam intervalHari ke depan.
     */
    public boolean cekJatuhTempo(Dokumen doc) {
        if (doc.getTanggalBerakhir() == null) return false;
        long sisaHari = doc.getSisaHari();
        return sisaHari >= 0 && sisaHari <= intervalHari;
    }

    /**
     * Format pesan notifikasi untuk dokumen.
     */
    public String formatPesan(Dokumen doc) {
        long sisaHari = doc.getSisaHari();
        if (sisaHari < 0) {
            return String.format(
                "⚠️ Dokumen '%s' telah KADALUARSA pada %s.",
                doc.getNamaDokumen(), doc.getTanggalBerakhir()
            );
        }
        return String.format(
            "📋 Dokumen '%s' akan berakhir dalam %d hari (tanggal: %s). Harap segera diperbarui.",
            doc.getNamaDokumen(), sisaHari, doc.getTanggalBerakhir()
        );
    }

    /**
     * Proses pengecekan: return true jika notifikasi perlu dikirim.
     */
    public boolean prosesPengecekan() {
        // Kirim notifikasi jika belum pernah dikirim hari ini
        if (lastSent == null) return true;
        return !lastSent.isEqual(LocalDate.now());
    }

    public void markAsSent() {
        this.lastSent = LocalDate.now();
    }
}
