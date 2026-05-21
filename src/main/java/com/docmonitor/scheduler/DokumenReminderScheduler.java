package com.docmonitor.scheduler;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.DokumenPeserta;
import com.docmonitor.model.SistemPengingat;
import com.docmonitor.repository.SistemPengingatRepository;
import com.docmonitor.service.DokumenService;
import com.docmonitor.service.EmailInviteService;
import com.docmonitor.service.EmailNotifikasi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DokumenReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(DokumenReminderScheduler.class);

    private final SistemPengingatRepository pengingatRepository;
    private final DokumenService dokumenService;
    private final EmailNotifikasi emailNotifikasi;
    private final EmailInviteService emailInviteService;

    // Constructor
    public DokumenReminderScheduler(SistemPengingatRepository pengingatRepository,
                                   DokumenService dokumenService, EmailNotifikasi emailNotifikasi,
                                   EmailInviteService emailInviteService) {
        this.pengingatRepository = pengingatRepository;
        this.dokumenService = dokumenService;
        this.emailNotifikasi = emailNotifikasi;
        this.emailInviteService = emailInviteService;
    }

    /**
     * CRON JOB - Jalankan setiap hari sesuai konfigurasi (default: 08:00 pagi)
     * Format: second minute hour day month weekday
     * "0 0 8 * * *" = setiap hari jam 08:00:00
     */
    @Scheduled(cron = "${app.scheduler.cron:0 0 8 * * *}")
    public void jalankanPengecekanHarian() {
        log.info("========================================================");
        log.info("[SCHEDULER] Mulai pengecekan dokumen - {}", LocalDateTime.now());
        log.info("========================================================");

        // 1. Update status semua dokumen
        updateStatusDokumen();

        // 2. Kirim notifikasi untuk dokumen yang akan/sudah berakhir
        kirimNotifikasiPengingat();

        log.info("[SCHEDULER] Selesai pengecekan dokumen - {}", LocalDateTime.now());
    }

    /**
     * Update status semua dokumen berdasarkan tanggal hari ini.
     */
    private void updateStatusDokumen() {
        log.info("[SCHEDULER] Step 1: Update status dokumen...");
        try {
            dokumenService.updateAllStatus();
            log.info("[SCHEDULER] Status dokumen berhasil diperbarui.");
        } catch (Exception e) {
            log.error("[SCHEDULER] Gagal update status dokumen: {}", e.getMessage());
        }
    }

    /**
     * Kirim notifikasi email untuk dokumen yang akan berakhir atau sudah kadaluarsa.
     */
    private void kirimNotifikasiPengingat() {
        log.info("[SCHEDULER] Step 2: Kirim notifikasi pengingat...");

        List<SistemPengingat> semuaPengingat = pengingatRepository.findAllWithDokumen();
        log.info("[SCHEDULER] Total pengingat aktif: {}", semuaPengingat.size());

        int berhasil = 0;
        int dilewati = 0;

        for (SistemPengingat pengingat : semuaPengingat) {
            try {
                Dokumen dokumen = pengingat.getDokumen();

                // Cek apakah perlu kirim notifikasi hari ini
                if (!pengingat.prosesPengecekan()) {
                    dilewati++;
                    continue;
                }

                // Cek apakah dokumen dalam rentang intervalHari
                if (pengingat.cekJatuhTempo(dokumen) || dokumen.getSisaHari() < 0) {
                    log.info("[SCHEDULER] Mengirim notifikasi untuk dokumen: '{}' (sisa {} hari)",
                        dokumen.getNamaDokumen(), dokumen.getSisaHari());

                    // Kirim email ke pemilik dokumen
                    emailNotifikasi.kirimNotifikasi(dokumen);

                    // Kirim notifikasi ke peserta (dokumen bersama)
                    List<DokumenPeserta> pesertaList = dokumenService.getPesertaByDokumen(dokumen.getDokumenId());
                    for (DokumenPeserta peserta : pesertaList) {
                        emailInviteService.kirimNotifikasiKePeserta(dokumen, peserta.getEmailPeserta());
                    }

                    // Update lastSent
                    pengingat.markAsSent();
                    pengingatRepository.save(pengingat);

                    berhasil++;
                } else {
                    dilewati++;
                }

            } catch (Exception e) {
                log.error("[SCHEDULER] Gagal proses pengingat ID {}: {}",
                    pengingat.getReminderId(), e.getMessage());
            }
        }

        log.info("[SCHEDULER] Notifikasi selesai: {} berhasil, {} dilewati", berhasil, dilewati);
    }

    /**
     * Jalankan pengecekan manual (bisa dipanggil dari controller).
     */
    public void jalankanManual() {
        log.info("[SCHEDULER] Pengecekan manual dijalankan oleh admin");
        jalankanPengecekanHarian();
    }
}
