package com.docmonitor.config;

import com.docmonitor.model.KategoriDokumen;
import com.docmonitor.model.User;
import com.docmonitor.repository.KategoriDokumenRepository;
import com.docmonitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final KategoriDokumenRepository kategoriRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor
    public DataInitializer(UserRepository userRepository, KategoriDokumenRepository kategoriRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.kategoriRepository = kategoriRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.admin.default-email:admin@docmonitor.com}")
    private String adminEmail;

    @Value("${app.admin.default-password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        initAdminUser();
        initDefaultKategori();
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                .name("Administrator")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("-")
                .role("ADMIN")
                .active(true)
                .build();
            userRepository.save(admin);
            log.info("✅ Admin user dibuat: {}", adminEmail);
        }
    }

    private void initDefaultKategori() {
        // ── Dokumen PRIBADI ──────────────────────────────────────────────────
        // Identitas
        List<String[]> identitas = List.of(
            new String[]{"Identitas - KTP", "Kartu Tanda Penduduk"},
            new String[]{"Identitas - Paspor", "Dokumen perjalanan internasional"},
            new String[]{"Identitas - SKCK", "Surat Keterangan Catatan Kepolisian"}
        );
        // Pendidikan
        List<String[]> pendidikan = List.of(
            new String[]{"Pendidikan - Ijazah", "Dokumen ijazah pendidikan formal"},
            new String[]{"Pendidikan - Transkrip", "Transkrip nilai akademik"},
            new String[]{"Pendidikan - Sertifikat Kursus", "Sertifikat kursus atau pelatihan pribadi"}
        );
        // Legalitas
        List<String[]> legalitas = List.of(
            new String[]{"Legalitas - Kontrak Kerja", "Dokumen kontrak kerja pribadi"},
            new String[]{"Legalitas - Izin Usaha", "Dokumen perizinan bisnis pribadi"},
            new String[]{"Legalitas - Asuransi", "Polis asuransi jiwa atau kesehatan"}
        );
        // Kendaraan
        List<String[]> kendaraan = List.of(
            new String[]{"Kendaraan - SIM", "Surat Izin Mengemudi"},
            new String[]{"Kendaraan - STNK", "Surat Tanda Nomor Kendaraan"},
            new String[]{"Kendaraan - BPKB", "Buku Pemilik Kendaraan Bermotor"}
        );
        // Keuangan
        List<String[]> keuangan = List.of(
            new String[]{"Keuangan - Rekening Bank", "Buku tabungan atau dokumen rekening bank"},
            new String[]{"Keuangan - NPWP", "Nomor Pokok Wajib Pajak"},
            new String[]{"Keuangan - Sertifikat Saham", "Dokumen kepemilikan saham atau investasi"}
        );

        for (List<String[]> group : List.of(identitas, pendidikan, legalitas, kendaraan, keuangan)) {
            for (String[] kat : group) {
                if (!kategoriRepository.existsByNamaKategoriIgnoreCase(kat[0])) {
                    KategoriDokumen kategori = KategoriDokumen.builder()
                        .namaKategori(kat[0])
                        .deskripsi(kat[1])
                        .tipe(com.docmonitor.model.TipeKategori.INDIVIDU)
                        .build();
                    kategoriRepository.save(kategori);
                }
            }
        }

        // ── Dokumen BERSAMA ──────────────────────────────────────────────────
        List<String[]> defaultKategoriBersama = List.of(
            new String[]{"Kepanitiaan", "Surat Keputusan kepanitiaan atau tim kerja"},
            new String[]{"Kontrak Bersama", "Dokumen kontrak yang melibatkan beberapa pihak"},
            new String[]{"Sertifikat Lomba", "Sertifikat penghargaan lomba tim atau kelompok"},
            new String[]{"Sertifikat Pelatihan", "Sertifikat pelatihan yang diikuti bersama"}
        );

        for (String[] kat : defaultKategoriBersama) {
            if (!kategoriRepository.existsByNamaKategoriIgnoreCase(kat[0])) {
                KategoriDokumen kategori = KategoriDokumen.builder()
                    .namaKategori(kat[0])
                    .deskripsi(kat[1])
                    .tipe(com.docmonitor.model.TipeKategori.BERSAMA)
                    .build();
                kategoriRepository.save(kategori);
            }
        }
        log.info("✅ Kategori dokumen default berhasil diinisialisasi.");
    }
}
