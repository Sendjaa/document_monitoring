package com.docmonitor.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kategori_dokumen")
public class KategoriDokumen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kategoriId;

    @Column(nullable = false, unique = true)
    private String namaKategori;

    @Column(columnDefinition = "TEXT")
    private String deskripsi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipeKategori tipe = TipeKategori.INDIVIDU;

    @OneToMany(mappedBy = "kategori", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dokumen> dokumenList = new ArrayList<>();

    // Constructors
    public KategoriDokumen() {}

    public KategoriDokumen(String namaKategori, String deskripsi) {
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
    }

    public static KategoriDokumenBuilder builder() {
        return new KategoriDokumenBuilder();
    }

    // Getters and Setters
    public Long getKategoriId() { return kategoriId; }
    public void setKategoriId(Long kategoriId) { this.kategoriId = kategoriId; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public TipeKategori getTipe() { return tipe; }
    public void setTipe(TipeKategori tipe) { this.tipe = tipe; }

    public List<Dokumen> getDokumenList() { return dokumenList; }
    public void setDokumenList(List<Dokumen> dokumenList) { this.dokumenList = dokumenList; }

    // Business methods
    public String getKategoriInfo() {
        return namaKategori + " - " + (deskripsi != null ? deskripsi : "Tidak ada deskripsi");
    }

    public int getDokumenCount() {
        return dokumenList != null ? dokumenList.size() : 0;
    }

    // Builder pattern
    public static class KategoriDokumenBuilder {
        private String namaKategori;
        private String deskripsi;
        private TipeKategori tipe = TipeKategori.INDIVIDU;

        public KategoriDokumenBuilder namaKategori(String namaKategori) {
            this.namaKategori = namaKategori;
            return this;
        }

        public KategoriDokumenBuilder deskripsi(String deskripsi) {
            this.deskripsi = deskripsi;
            return this;
        }

        public KategoriDokumenBuilder tipe(TipeKategori tipe) {
            this.tipe = tipe;
            return this;
        }

        public KategoriDokumen build() {
            KategoriDokumen k = new KategoriDokumen(namaKategori, deskripsi);
            k.setTipe(tipe);
            return k;
        }
    }
}
