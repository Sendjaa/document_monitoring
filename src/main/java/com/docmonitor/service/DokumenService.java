package com.docmonitor.service;

import com.docmonitor.dto.DokumenExtractDTO;
import com.docmonitor.dto.DokumenRequestDTO;
import com.docmonitor.model.*;
import com.docmonitor.repository.*;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DokumenService {

    private static final Logger log = LoggerFactory.getLogger(DokumenService.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final DokumenRepository dokumenRepository;
    private final KategoriDokumenRepository kategoriRepository;
    private final SistemPengingatRepository sistemPengingatRepository;
    private final GeminiVisionService geminiVisionService;
    private final DokumenPesertaRepository dokumenPesertaRepository;
    private final EmailInviteService emailInviteService;

    // Constructor
    public DokumenService(DokumenRepository dokumenRepository, UserRepository userRepository,
            KategoriDokumenRepository kategoriRepository, SistemPengingatRepository sistemPengingatRepository,
            GeminiVisionService geminiVisionService, DokumenPesertaRepository dokumenPesertaRepository,
            EmailInviteService emailInviteService) {
        this.dokumenRepository = dokumenRepository;
        this.kategoriRepository = kategoriRepository;
        this.sistemPengingatRepository = sistemPengingatRepository;
        this.geminiVisionService = geminiVisionService;
        this.dokumenPesertaRepository = dokumenPesertaRepository;
        this.emailInviteService = emailInviteService;
    }

    @Value("${app.scheduler.warning-days:30}")
    private int warningDays;

    // =============================================
    // CRUD Operations
    // =============================================

    public Dokumen simpanDokumen(DokumenRequestDTO dto, User user, MultipartFile file) throws IOException {
        Dokumen dokumen = new Dokumen();
        dokumen.setNamaDokumen(dto.getNamaDokumen());
        dokumen.setTanggalMulai(dto.getTanggalMulai());
        dokumen.setTanggalBerakhir(dto.getTanggalBerakhir());
        dokumen.setUser(user);

        if (dto.getKategoriId() != null) {
            kategoriRepository.findById(dto.getKategoriId())
                    .ifPresent(dokumen::setKategori);
        }

        if (file != null && !file.isEmpty()) {
            String savedPath = saveFile(file);
            dokumen.uploadFile(savedPath);
        }

        dokumen.cekStatus(warningDays);

        Dokumen saved = dokumenRepository.save(dokumen);

        if (dto.getEmailPeserta() != null && !dto.getEmailPeserta().isEmpty()) {
            java.util.List<String> validEmails = new java.util.ArrayList<>();
            java.util.List<String> validTokens = new java.util.ArrayList<>();
            for (String email : dto.getEmailPeserta()) {
                if (email != null && !email.isBlank()) {
                    String token = UUID.randomUUID().toString();
                    DokumenPeserta peserta = new DokumenPeserta(saved, email.trim(), null);
                    peserta.setInviteToken(token);
                    peserta.setAccepted(false);
                    dokumenPesertaRepository.save(peserta);
                    validEmails.add(email.trim());
                    validTokens.add(token);
                }
            }
            if (!validEmails.isEmpty()) {
                emailInviteService.kirimUndanganPeserta(saved.getDokumenId(), user, validEmails, validTokens);
            }
        }

        buatPengingat(saved, dto.getIntervalHari() != null ? dto.getIntervalHari() : 30);

        log.info("Dokumen berhasil disimpan: ID={}, Nama={}", saved.getDokumenId(), saved.getNamaDokumen());
        return saved;
    }

    public Dokumen simpanDariEkstraksi(DokumenExtractDTO extractDTO, User user, MultipartFile imageFile)
            throws IOException {
        Dokumen dokumen = new Dokumen();
        dokumen.setNamaDokumen(extractDTO.getNamaDokumen());
        dokumen.setTanggalMulai(extractDTO.getTanggalMulai());
        dokumen.setTanggalBerakhir(extractDTO.getTanggalBerakhir());
        dokumen.setUser(user);

        if (extractDTO.getNamaKategori() != null && !extractDTO.getNamaKategori().isBlank()) {
            KategoriDokumen kategori = kategoriRepository
                    .findByNamaKategoriIgnoreCase(extractDTO.getNamaKategori())
                    .orElseGet(() -> {
                        KategoriDokumen newKat = KategoriDokumen.builder()
                                .namaKategori(extractDTO.getNamaKategori())
                                .deskripsi("Dibuat otomatis dari ekstraksi LLM")
                                .tipe(TipeKategori.INDIVIDU)
                                .build();
                        return kategoriRepository.save(newKat);
                    });
            dokumen.setKategori(kategori);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String savedPath = saveFile(imageFile);
            dokumen.uploadFile(savedPath);
        }

        dokumen.cekStatus(warningDays);

        Dokumen saved = dokumenRepository.save(dokumen);

        buatPengingat(saved, 30);

        log.info("Dokumen dari LLM berhasil disimpan: ID={}, Nama={}", saved.getDokumenId(), saved.getNamaDokumen());
        return saved;
    }

    public Dokumen updateDokumen(Long id, DokumenRequestDTO dto, MultipartFile file) throws IOException {
        Dokumen dokumen = findById(id);
        dokumen.setNamaDokumen(dto.getNamaDokumen());
        dokumen.setTanggalMulai(dto.getTanggalMulai());
        dokumen.setTanggalBerakhir(dto.getTanggalBerakhir());

        if (dto.getKategoriId() != null) {
            kategoriRepository.findById(dto.getKategoriId())
                    .ifPresent(dokumen::setKategori);
        }

        if (file != null && !file.isEmpty()) {
            if (dokumen.getFilePath() != null)
                deleteFile(dokumen.getFilePath());
            String savedPath = saveFile(file);
            dokumen.uploadFile(savedPath);
        }

        dokumen.cekStatus(warningDays);

        if (dto.getIntervalHari() != null) {
            sistemPengingatRepository.findByDokumenDokumenId(id).ifPresent(p -> {
                p.setIntervalHari(dto.getIntervalHari());
                sistemPengingatRepository.save(p);
            });
        }

        return dokumenRepository.save(dokumen);
    }

    @Transactional // Wajib agar semua proses dianggap satu kesatuan
    public void hapusDokumen(Long id) {
        // 1. Ambil dokumen untuk pengecekan file
        Dokumen dokumen = findById(id);

        // 2. Hapus file fisik (sudah ada di kode Anda)
        if (dokumen.getFilePath() != null) {
            deleteFile(dokumen.getFilePath());
        }

        // Pastikan Anda sudah inject repository yang dibutuhkan
        sistemPengingatRepository.deleteByDokumen_DokumenId(id);
        dokumenPesertaRepository.deleteByDokumen_DokumenId(id);

        // 4. Terakhir, hapus entitas dokumen
        dokumenRepository.delete(dokumen);

        log.info("Dokumen ID={} beserta data terkait berhasil dihapus", id);
    }

    public Dokumen findById(Long id) {
        return dokumenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokumen tidak ditemukan dengan ID: " + id));
    }

    public List<Dokumen> findByUser(Long userId) {
        return dokumenRepository.findByUserUserId(userId);
    }

    public List<Dokumen> findByStatus(StatusDokumen status) {
        return dokumenRepository.findByStatus(status);
    }

    public List<Dokumen> searchDokumen(String keyword, Long userId) {
        return dokumenRepository.searchByNamaDokumen(keyword, userId);
    }

    public List<Dokumen> findAll() {
        return dokumenRepository.findAll();
    }

    // =============================================
    // LLM Extraction
    // =============================================

    public DokumenExtractDTO ekstrakDariGambar(MultipartFile imageFile) throws IOException {
        return geminiVisionService.ekstrakDariGambar(imageFile);
    }

    // =============================================
    // Kolaborasi & Status
    // =============================================

    public List<Dokumen> findDokumenKolaborasi(Long userId) {
        return dokumenPesertaRepository.findByUser_UserIdAndAcceptedTrue(userId)
                .stream()
                .map(DokumenPeserta::getDokumen)
                .collect(Collectors.toList());
    }

    public void updateAllStatus() {
        List<Dokumen> allDokumen = dokumenRepository.findAll();
        for (Dokumen d : allDokumen) {
            StatusDokumen oldStatus = d.getStatus();
            d.cekStatus(warningDays);
            if (!oldStatus.equals(d.getStatus())) {
                dokumenRepository.save(d);
                log.info("Status dokumen '{}' berubah dari {} ke {}",
                        d.getNamaDokumen(), oldStatus, d.getStatus());
            }
        }
    }

    public long countByStatus(StatusDokumen status) {
        return dokumenRepository.countByStatus(status);
    }

    public long countByUser(Long userId) {
        return dokumenRepository.countByUserUserId(userId);
    }

    public List<DokumenPeserta> getPesertaByDokumen(Long dokumenId) {
        return dokumenPesertaRepository.findByDokumen_DokumenId(dokumenId);
    }

    public void tambahPeserta(Long dokumenId, String email) {
        Dokumen dokumen = findById(dokumenId);
        if (!dokumenPesertaRepository.existsByDokumen_DokumenIdAndEmailPeserta(dokumenId, email)) {
            dokumenPesertaRepository.save(new DokumenPeserta(dokumen, email, null));
        }
    }

    @Transactional
    public void hapusPeserta(Long dokumenId, String email) {
        dokumenPesertaRepository.deleteByDokumen_DokumenIdAndEmailPeserta(dokumenId, email);
    }

    // =============================================
    // Private Helpers
    // =============================================

    private void buatPengingat(Dokumen dokumen, int intervalHari) {
        SistemPengingat pengingat = SistemPengingat.builder()
                .dokumen(dokumen)
                .intervalHari(intervalHari)
                .build();
        sistemPengingatRepository.save(pengingat);
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return uploadDir + "/" + filename;
    }

    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Gagal hapus file: {}", filePath);
        }
    }
}