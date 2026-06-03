package com.docmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifikasi")
public class Notifikasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notifikasiId;

    @Column(nullable = false)
    private String judul;

    @Column(columnDefinition = "TEXT")
    private String pesan;

    @Column(nullable = false)
    private String tipe;

    private boolean dibaca = false;

    @Column(nullable = false)
    private LocalDateTime dibuatPada = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dokumen_id")
    private Dokumen dokumen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors
    public Notifikasi() {}

    public Notifikasi(String judul, String pesan, String tipe, Dokumen dokumen, User user) {
        this.judul = judul;
        this.pesan = pesan;
        this.tipe = tipe;
        this.dokumen = dokumen;
        this.user = user;
        this.dibuatPada = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getNotifikasiId() { return notifikasiId; }
    public void setNotifikasiId(Long notifikasiId) { this.notifikasiId = notifikasiId; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getPesan() { return pesan; }
    public void setPesan(String pesan) { this.pesan = pesan; }

    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }

    public boolean isDibaca() { return dibaca; }
    public void setDibaca(boolean dibaca) { this.dibaca = dibaca; }

    public LocalDateTime getDibuatPada() { return dibuatPada; }
    public void setDibuatPada(LocalDateTime dibuatPada) { this.dibuatPada = dibuatPada; }

    public Dokumen getDokumen() { return dokumen; }
    public void setDokumen(Dokumen dokumen) { this.dokumen = dokumen; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}