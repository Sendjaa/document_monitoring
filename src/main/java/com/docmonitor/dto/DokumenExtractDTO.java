package com.docmonitor.dto;

import java.time.LocalDate;

public class DokumenExtractDTO {

    private String extractedText;
    private String namaDokumen;
    private String kategori;
    private LocalDate tanggalBerakhir;
    private String confidence;
    private String namaKategori;
    private String deskripsiTambahan;
    private LocalDate tanggalMulai;

    // Constructors
    public DokumenExtractDTO() {}

    public DokumenExtractDTO(String extractedText, String namaDokumen, String kategori, LocalDate tanggalBerakhir, String confidence) {
        this.extractedText = extractedText;
        this.namaDokumen = namaDokumen;
        this.kategori = kategori;
        this.tanggalBerakhir = tanggalBerakhir;
        this.confidence = confidence;
    }

    public static DokumenExtractDTOBuilder builder() {
        return new DokumenExtractDTOBuilder();
    }

    // Getters and Setters
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getNamaDokumen() { return namaDokumen; }
    public void setNamaDokumen(String namaDokumen) { this.namaDokumen = namaDokumen; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public LocalDate getTanggalBerakhir() { return tanggalBerakhir; }
    public void setTanggalBerakhir(LocalDate tanggalBerakhir) { this.tanggalBerakhir = tanggalBerakhir; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getDeskripsiTambahan() { return deskripsiTambahan; }
    public void setDeskripsiTambahan(String deskripsiTambahan) { this.deskripsiTambahan = deskripsiTambahan; }

    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    // Builder pattern
    public static class DokumenExtractDTOBuilder {
        private String extractedText;
        private String namaDokumen;
        private String kategori;
        private LocalDate tanggalBerakhir;
        private String confidence;

        public DokumenExtractDTOBuilder extractedText(String extractedText) {
            this.extractedText = extractedText;
            return this;
        }

        public DokumenExtractDTOBuilder namaDokumen(String namaDokumen) {
            this.namaDokumen = namaDokumen;
            return this;
        }

        public DokumenExtractDTOBuilder kategori(String kategori) {
            this.kategori = kategori;
            return this;
        }

        public DokumenExtractDTOBuilder tanggalBerakhir(LocalDate tanggalBerakhir) {
            this.tanggalBerakhir = tanggalBerakhir;
            return this;
        }

        public DokumenExtractDTOBuilder confidence(String confidence) {
            this.confidence = confidence;
            return this;
        }

        public DokumenExtractDTO build() {
            return new DokumenExtractDTO(extractedText, namaDokumen, kategori, tanggalBerakhir, confidence);
        }
    }
}
