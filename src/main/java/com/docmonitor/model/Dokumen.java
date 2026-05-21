package com.docmonitor.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "dokumen")
public class Dokumen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dokumenId;

    @Column(nullable = false)
    private String namaDokumen;

    private LocalDate tanggalMulai;

    private LocalDate tanggalBerakhir;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDokumen status = StatusDokumen.AKTIF;

    @Column(columnDefinition = "TEXT")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_id")
    private KategoriDokumen kategori;

    @OneToOne(mappedBy = "dokumen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SistemPengingat sistemPengingat;

    @OneToMany(mappedBy = "dokumen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.List<DokumenPeserta> pesertaList = new java.util.ArrayList<>();

    // Constructors
    public Dokumen() {}

    public Dokumen(String namaDokumen, LocalDate tanggalMulai, LocalDate tanggalBerakhir, 
                   StatusDokumen status, String filePath, User user, KategoriDokumen kategori) {
        this.namaDokumen = namaDokumen;
        this.tanggalMulai = tanggalMulai;
        this.tanggalBerakhir = tanggalBerakhir;
        this.status = status;
        this.filePath = filePath;
        this.user = user;
        this.kategori = kategori;
    }

    // Getters and Setters
    public Long getDokumenId() { return dokumenId; }
    public void setDokumenId(Long dokumenId) { this.dokumenId = dokumenId; }

    public String getNamaDokumen() { return namaDokumen; }
    public void setNamaDokumen(String namaDokumen) { this.namaDokumen = namaDokumen; }

    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public LocalDate getTanggalBerakhir() { return tanggalBerakhir; }
    public void setTanggalBerakhir(LocalDate tanggalBerakhir) { this.tanggalBerakhir = tanggalBerakhir; }

    public StatusDokumen getStatus() { return status; }
    public void setStatus(StatusDokumen status) { this.status = status; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public KategoriDokumen getKategori() { return kategori; }
    public void setKategori(KategoriDokumen kategori) { this.kategori = kategori; }

    public SistemPengingat getSistemPengingat() { return sistemPengingat; }
    public void setSistemPengingat(SistemPengingat sistemPengingat) { this.sistemPengingat = sistemPengingat; }

    public java.util.List<DokumenPeserta> getPesertaList() { return pesertaList; }
    public void setPesertaList(java.util.List<DokumenPeserta> pesertaList) { this.pesertaList = pesertaList; }

    // =============================================
    // Business methods
    // =============================================

    /**
     * Cek dan update status berdasarkan tanggal berakhir.
     */
    public StatusDokumen cekStatus(int warningDays) {
        if (tanggalBerakhir == null) return StatusDokumen.AKTIF;

        LocalDate today = LocalDate.now();
        long hariSisa = ChronoUnit.DAYS.between(today, tanggalBerakhir);

        if (hariSisa < 0) {
            this.status = StatusDokumen.KADALUARSA;
        } else if (hariSisa <= warningDays) {
            this.status = StatusDokumen.AKAN_HABIS;
        } else {
            this.status = StatusDokumen.AKTIF;
        }
        return this.status;
    }

    public void updateStatus(StatusDokumen newStatus) {
        this.status = newStatus;
    }

    public void uploadFile(String path) {
        this.filePath = path;
    }

    public void hapusFile() {
        this.filePath = null;
    }

    /**
     * Hitung sisa hari sebelum expired.
     */
    public long getSisaHari() {
        if (tanggalBerakhir == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), tanggalBerakhir);
    }
}
