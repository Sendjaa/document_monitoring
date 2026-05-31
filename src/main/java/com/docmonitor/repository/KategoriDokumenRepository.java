package com.docmonitor.repository;

import com.docmonitor.model.KategoriDokumen;
import com.docmonitor.model.TipeKategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KategoriDokumenRepository extends JpaRepository<KategoriDokumen, Long> {

    /**
     * Cari kategori berdasarkan nama.
     */
    Optional<KategoriDokumen> findByNamaKategori(String namaKategori);

    /**
     * Cari kategori berdasarkan nama (case insensitive).
     */
    Optional<KategoriDokumen> findByNamaKategoriIgnoreCase(String namaKategori);

    /**
     * Cek apakah nama kategori sudah ada (case insensitive).
     */
    boolean existsByNamaKategoriIgnoreCase(String namaKategori);

    /**
     * Cari kategori berdasarkan tipe (INDIVIDU / KELOMPOK).
     */
    List<KategoriDokumen> findByTipe(TipeKategori tipe);
}
