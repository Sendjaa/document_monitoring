package com.docmonitor.dto;

import com.docmonitor.model.StatusDokumen;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class DokumenRequestDTO {

    @NotBlank(message = "Nama dokumen wajib diisi")
    private String namaDokumen;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulai;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalBerakhir;

    private StatusDokumen status;

    private Long kategoriId;

    // Interval hari untuk pengingat (default 30)
    private Integer intervalHari = 30;

    // Constructors
    public DokumenRequestDTO() {}

    public DokumenRequestDTO(String namaDokumen, LocalDate tanggalMulai, LocalDate tanggalBerakhir, 
                            StatusDokumen status, Long kategoriId, Integer intervalHari) {
        this.namaDokumen = namaDokumen;
        this.tanggalMulai = tanggalMulai;
        this.tanggalBerakhir = tanggalBerakhir;
        this.status = status;
        this.kategoriId = kategoriId;
        this.intervalHari = intervalHari;
    }

    // Getters and Setters
    public String getNamaDokumen() { return namaDokumen; }
    public void setNamaDokumen(String namaDokumen) { this.namaDokumen = namaDokumen; }

    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public LocalDate getTanggalBerakhir() { return tanggalBerakhir; }
    public void setTanggalBerakhir(LocalDate tanggalBerakhir) { this.tanggalBerakhir = tanggalBerakhir; }

    public StatusDokumen getStatus() { return status; }
    public void setStatus(StatusDokumen status) { this.status = status; }

    public Long getKategoriId() { return kategoriId; }
    public void setKategoriId(Long kategoriId) { this.kategoriId = kategoriId; }

    public Integer getIntervalHari() { return intervalHari; }
    public void setIntervalHari(Integer intervalHari) { this.intervalHari = intervalHari; }
}