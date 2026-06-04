package com.docmonitor.repository;

import com.docmonitor.model.Notifikasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {
    // Ambil semua notifikasi milik user (untuk ditampilkan di navbar/dashboard)
    List<Notifikasi> findByUser_UserIdOrderByDibuatPadaDesc(Long userId);

    // Ambil yang belum dibaca
    List<Notifikasi> findByUser_UserIdAndDibacaFalse(Long userId);

    // Hapus saat dokumen dihapus
    void deleteByDokumen_DokumenId(Long dokumenId);

    // Hitung notifikasi belum dibaca
    long countByUser_UserIdAndDibacaFalse(Long userId);
}