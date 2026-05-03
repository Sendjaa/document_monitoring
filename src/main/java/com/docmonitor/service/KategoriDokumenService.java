package com.docmonitor.service;

import com.docmonitor.model.KategoriDokumen;
import com.docmonitor.repository.KategoriDokumenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class KategoriDokumenService {

    private final KategoriDokumenRepository kategoriRepository;

    // Constructor
    public KategoriDokumenService(KategoriDokumenRepository kategoriRepository) {
        this.kategoriRepository = kategoriRepository;
    }

    public KategoriDokumen simpan(String namaKategori, String deskripsi) {
        if (kategoriRepository.existsByNamaKategoriIgnoreCase(namaKategori)) {
            throw new RuntimeException("Kategori sudah ada: " + namaKategori);
        }
        KategoriDokumen kategori = KategoriDokumen.builder()
            .namaKategori(namaKategori)
            .deskripsi(deskripsi)
            .build();
        return kategoriRepository.save(kategori);
    }

    public KategoriDokumen update(Long id, String namaKategori, String deskripsi) {
        KategoriDokumen kategori = findById(id);
        kategori.setNamaKategori(namaKategori);
        kategori.setDeskripsi(deskripsi);
        return kategoriRepository.save(kategori);
    }

    public void hapus(Long id) {
        kategoriRepository.deleteById(id);
    }

    public KategoriDokumen findById(Long id) {
        return kategoriRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan ID: " + id));
    }

    public List<KategoriDokumen> findAll() {
        return kategoriRepository.findAll();
    }
}
