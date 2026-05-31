package com.docmonitor.repository;

import com.docmonitor.model.DokumenPeserta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DokumenPesertaRepository extends JpaRepository<DokumenPeserta, Long> {
    @Modifying
    List<DokumenPeserta> findByDokumen_DokumenId(Long dokumenId);
    void deleteByDokumen_DokumenIdAndEmailPeserta(Long dokumenId, String emailPeserta);
    void deleteByDokumen_DokumenId(Long dokumenId);
    boolean existsByDokumen_DokumenIdAndEmailPeserta(Long dokumenId, String emailPeserta);
    
    List<DokumenPeserta> findByUserUserId(Long userId);
    Optional<DokumenPeserta> findByInviteToken(String inviteToken);
    List<DokumenPeserta> findByUser_UserIdAndAcceptedTrue(Long userId);
    List<DokumenPeserta> findByEmailPesertaAndAcceptedFalse(String emailPeserta);
}
