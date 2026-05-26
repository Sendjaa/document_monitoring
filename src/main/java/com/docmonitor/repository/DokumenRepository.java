package com.docmonitor.repository;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.StatusDokumen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph; // Pastikan import ini ada

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Pastikan import ini ada

@Repository
public interface DokumenRepository extends JpaRepository<Dokumen, Long> {

    /**
     * Memaksa fetch data kategori dan user agar tidak terjadi error "no Session"
     * saat diakses di thread asinkron (@Async).
     */
    @Override
    @EntityGraph(attributePaths = {"kategori", "user"})
    Optional<Dokumen> findById(Long id);

    /**
     * Cari semua dokumen milik user tertentu.
     */
    List<Dokumen> findByUserUserId(Long userId);

    /**
     * Cari dokumen berdasarkan status.
     */
    List<Dokumen> findByStatus(StatusDokumen status);

    /**
     * Cari dokumen yang akan berakhir dalam N hari ke depan.
     */
    @Query("SELECT d FROM Dokumen d WHERE d.tanggalBerakhir BETWEEN :today AND :targetDate AND d.status != 'KADALUARSA'")
    List<Dokumen> findDokumenAkanBerakhir(
        @Param("today") LocalDate today,
        @Param("targetDate") LocalDate targetDate
    );

    /**
     * Cari dokumen yang sudah kadaluarsa.
     */
    @Query("SELECT d FROM Dokumen d WHERE d.tanggalBerakhir < :today AND d.status != 'KADALUARSA'")
    List<Dokumen> findDokumenKadaluarsa(@Param("today") LocalDate today);

    /**
     * Cari dokumen berdasarkan user dan status.
     */
    List<Dokumen> findByUserUserIdAndStatus(Long userId, StatusDokumen status);

    /**
     * Cari dokumen berdasarkan kategori.
     */
    List<Dokumen> findByKategoriKategoriId(Long kategoriId);

    /**
     * Hitung total dokumen per user.
     */
    long countByUserUserId(Long userId);

    /**
     * Hitung dokumen berdasarkan status.
     */
    long countByStatus(StatusDokumen status);

    /**
     * Search dokumen berdasarkan nama.
     */
    @Query("SELECT d FROM Dokumen d WHERE LOWER(d.namaDokumen) LIKE LOWER(CONCAT('%', :keyword, '%')) AND d.user.userId = :userId")
    List<Dokumen> searchByNamaDokumen(@Param("keyword") String keyword, @Param("userId") Long userId);
}