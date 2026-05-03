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
        List<String[]> defaultKategori = List.of(
            new String[]{"KTP", "Kartu Tanda Penduduk"},
            new String[]{"SIM", "Surat Izin Mengemudi"},
            new String[]{"Paspor", "Dokumen perjalanan internasional"},
            new String[]{"STNK", "Surat Tanda Nomor Kendaraan"},
            new String[]{"SKCK", "Surat Keterangan Catatan Kepolisian"},
            new String[]{"Kontrak", "Dokumen kontrak perjanjian"},
            new String[]{"Sertifikat", "Sertifikat atau piagam"},
            new String[]{"Ijazah", "Dokumen pendidikan"},
            new String[]{"Asuransi", "Polis asuransi"},
            new String[]{"Izin Usaha", "Dokumen perizinan bisnis"}
        );

        for (String[] kat : defaultKategori) {
            if (!kategoriRepository.existsByNamaKategoriIgnoreCase(kat[0])) {
                KategoriDokumen kategori = KategoriDokumen.builder()
                    .namaKategori(kat[0])
                    .deskripsi(kat[1])
                    .build();
                kategoriRepository.save(kategori);
            }
        }
        log.info("✅ Kategori dokumen default berhasil diinisialisasi.");
    }
}
