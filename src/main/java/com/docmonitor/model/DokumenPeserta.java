package com.docmonitor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dokumen_peserta", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"dokumen_id", "email_peserta"})
})
public class DokumenPeserta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dokumen_id", nullable = false)
    private Dokumen dokumen;

    @Column(name = "email_peserta", nullable = false)
    private String emailPeserta;

    @Column(name = "nama_peserta")
    private String namaPeserta;

    // Constructors
    public DokumenPeserta() {}

    public DokumenPeserta(Dokumen dokumen, String emailPeserta, String namaPeserta) {
        this.dokumen = dokumen;
        this.emailPeserta = emailPeserta;
        this.namaPeserta = namaPeserta;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Dokumen getDokumen() { return dokumen; }
    public void setDokumen(Dokumen dokumen) { this.dokumen = dokumen; }

    public String getEmailPeserta() { return emailPeserta; }
    public void setEmailPeserta(String emailPeserta) { this.emailPeserta = emailPeserta; }

    public String getNamaPeserta() { return namaPeserta; }
    public void setNamaPeserta(String namaPeserta) { this.namaPeserta = namaPeserta; }
}
