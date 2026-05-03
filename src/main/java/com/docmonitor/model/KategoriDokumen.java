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

    public List<Dokumen> getDokumenList() { return dokumenList; }
    public void setDokumenList(List<Dokumen> dokumenList) { this.dokumenList = dokumenList; }

    // Business method
    public String getKategoriInfo() {
        return namaKategori + " - " + (deskripsi != null ? deskripsi : "Tidak ada deskripsi");
    }

    // Builder pattern
    public static class KategoriDokumenBuilder {
        private String namaKategori;
        private String deskripsi;

        public KategoriDokumenBuilder namaKategori(String namaKategori) {
            this.namaKategori = namaKategori;
            return this;
        }

        public KategoriDokumenBuilder deskripsi(String deskripsi) {
            this.deskripsi = deskripsi;
            return this;
        }

        public KategoriDokumen build() {
            return new KategoriDokumen(namaKategori, deskripsi);
        }
    }
}
