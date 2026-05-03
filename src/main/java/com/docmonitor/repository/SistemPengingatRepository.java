package com.docmonitor.repository;

import com.docmonitor.model.SistemPengingat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SistemPengingatRepository extends JpaRepository<SistemPengingat, Long> {

    Optional<SistemPengingat> findByDokumenDokumenId(Long dokumenId);

    /**
     * Ambil semua pengingat yang dokumennya akan berakhir dan perlu dikirim notifikasi.
     */
    @Query("""
        SELECT sp FROM SistemPengingat sp
        JOIN sp.dokumen d
        WHERE d.tanggalBerakhir IS NOT NULL
        AND d.user IS NOT NULL
        """)
    List<SistemPengingat> findAllWithDokumen();
}
