package com.docmonitor.controller;

import com.docmonitor.dto.DokumenExtractDTO;
import com.docmonitor.dto.DokumenRequestDTO;
import com.docmonitor.model.*;
import com.docmonitor.service.DokumenService;
import com.docmonitor.service.KategoriDokumenService;
import com.docmonitor.service.UserService;
import com.docmonitor.service.EmailInviteService;
import com.docmonitor.service.GeminiVisionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dokumen")
public class DokumenController {

    private static final Logger log = LoggerFactory.getLogger(DokumenController.class);

    private final DokumenService dokumenService;
    private final KategoriDokumenService kategoriService;
    private final UserService userService;
    private final EmailInviteService emailInviteService;
    private final GeminiVisionService geminiVisionService;

    public DokumenController(DokumenService dokumenService, KategoriDokumenService kategoriService,
            UserService userService, EmailInviteService emailInviteService, GeminiVisionService geminiVisionService) {
        this.dokumenService = dokumenService;
        this.kategoriService = kategoriService;
        this.userService = userService;
        this.emailInviteService = emailInviteService;
        this.geminiVisionService = geminiVisionService;
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    private void addCommonAttributes(Model model, User currentUser, String activeMenu) {
        model.addAttribute("currentUser", currentUser);
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("activeMenu", activeMenu);

        model.addAttribute("totalUsers", 0);
        model.addAttribute("totalDokumen", 0L);
        model.addAttribute("totalKategori", 0);
        model.addAttribute("dokumenAktif", 0L);
        model.addAttribute("dokumenAkanHabis", 0L);
        model.addAttribute("dokumenKadaluarsa", 0L);

        if (currentUser != null && currentUser.getUserId() != null) {
            Long userId = currentUser.getUserId();
            if (isAdmin) {
                model.addAttribute("totalUsers", userService.findAll().size());
                model.addAttribute("totalDokumen", dokumenService.findAll().size());
                model.addAttribute("totalKategori", kategoriService.findAll().size());
            } else {
                model.addAttribute("totalDokumen", dokumenService.countByUser(userId));
            }
            model.addAttribute("dokumenAktif", dokumenService.countByStatus(StatusDokumen.AKTIF));
            model.addAttribute("dokumenAkanHabis", dokumenService.countByStatus(StatusDokumen.AKAN_HABIS));
            model.addAttribute("dokumenKadaluarsa", dokumenService.countByStatus(StatusDokumen.KADALUARSA));
        }
    }

    private void addGroupedKategori(Model model) {
        List<KategoriDokumen> allKategori = kategoriService.findAll();

        Map<String, List<KategoriDokumen>> groupedKategori = new LinkedHashMap<>();
        for (TipeKategori tipe : TipeKategori.values()) {
            groupedKategori.put(tipe.getDisplayName(), new ArrayList<>());
        }
        for (KategoriDokumen k : allKategori) {
            if (k.getTipe() != null) {
                groupedKategori.computeIfAbsent(k.getTipe().getDisplayName(), key -> new ArrayList<>()).add(k);
            }
        }

        List<String> subGroupPribadi = Arrays.asList("Identitas", "Pendidikan", "Legalitas", "Kendaraan", "Keuangan");
        Map<String, List<KategoriDokumen>> subGroupedPribadi = new LinkedHashMap<>();
        for (String sg : subGroupPribadi) {
            subGroupedPribadi.put(sg, new ArrayList<>());
        }

        for (KategoriDokumen k : groupedKategori.getOrDefault("Individu", new ArrayList<>())) {
            boolean matched = false;
            for (String sg : subGroupPribadi) {
                if (k.getNamaKategori().startsWith(sg + " - ") || k.getNamaKategori().equalsIgnoreCase(sg)) {
                    subGroupedPribadi.get(sg).add(k);
                    matched = true;
                    break;
                }
            }
            if (!matched) subGroupedPribadi.get("Identitas").add(k);
        }

        model.addAttribute("groupedKategori", groupedKategori);
        model.addAttribute("subGroupedPribadi", subGroupedPribadi);
        model.addAttribute("subGroupPribadiKeys", subGroupPribadi);
        model.addAttribute("kategoriBersama", groupedKategori.getOrDefault("Bersama", new ArrayList<>()));
        model.addAttribute("allKategori", allKategori);
        model.addAttribute("kategoriList", allKategori);
        model.addAttribute("allStatus", StatusDokumen.values());
    }

    // =============================================
    // INDEX - Daftar Dokumen
    // =============================================

    @GetMapping
    public String index(@AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            Model model) {

        log.info("Mengakses daftar dokumen untuk user: {}", currentUser.getEmail());
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        List<Dokumen> dokumenPribadi;
        List<Dokumen> dokumenBersama;

        if (keyword != null && !keyword.isBlank()) {
            // Mode search: tampilkan semua hasil tanpa pemisahan
            List<Dokumen> hasil = dokumenService.searchDokumen(keyword, currentUser.getUserId());
            dokumenPribadi = hasil.stream()
                    .filter(d -> d.getKategori() == null ||
                                 d.getKategori().getTipe() == TipeKategori.INDIVIDU)
                    .collect(Collectors.toList());
            dokumenBersama = hasil.stream()
                    .filter(d -> d.getKategori() != null &&
                                 d.getKategori().getTipe() == TipeKategori.BERSAMA)
                    .collect(Collectors.toList());

        } else if (isAdmin) {
            // Admin: lihat semua dokumen
            List<Dokumen> semua = dokumenService.findAll();
            dokumenPribadi = semua.stream()
                    .filter(d -> d.getKategori() == null ||
                                 d.getKategori().getTipe() == TipeKategori.INDIVIDU)
                    .collect(Collectors.toList());
            dokumenBersama = semua.stream()
                    .filter(d -> d.getKategori() != null &&
                                 d.getKategori().getTipe() == TipeKategori.BERSAMA)
                    .collect(Collectors.toList());

        } else {
            // User biasa: pisah pribadi vs bersama
            List<Dokumen> milikSendiri = dokumenService.findByUser(currentUser.getUserId());

            dokumenPribadi = milikSendiri.stream()
                    .filter(d -> d.getKategori() == null ||
                                 d.getKategori().getTipe() == TipeKategori.INDIVIDU)
                    .collect(Collectors.toList());

            // Dokumen bersama milik sendiri + kolaborasi dari orang lain
            dokumenBersama = milikSendiri.stream()
                    .filter(d -> d.getKategori() != null &&
                                 d.getKategori().getTipe() == TipeKategori.BERSAMA)
                    .collect(Collectors.toList());

            List<Dokumen> kolaborasi = dokumenService.findDokumenKolaborasi(currentUser.getUserId());
            dokumenBersama.addAll(kolaborasi);
        }

        model.addAttribute("dokumenList", dokumenPribadi);
        model.addAttribute("dokumenKolaborasi", dokumenBersama);
        model.addAttribute("keyword", keyword);
        addCommonAttributes(model, currentUser, "dokumen");
        return "dokumen/list";
    }

    // =============================================
    // SHOW - Detail Dokumen
    // =============================================

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model,
            @AuthenticationPrincipal User currentUser) {
        Dokumen dokumen = dokumenService.findById(id);
        boolean isBersama = dokumen.getKategori() != null
                && TipeKategori.BERSAMA.equals(dokumen.getKategori().getTipe());

        model.addAttribute("dokumen", dokumen);
        model.addAttribute("pesertaList", dokumenService.getPesertaByDokumen(id));
        model.addAttribute("isBersama", isBersama);
        addCommonAttributes(model, currentUser, "dokumen");
        return "dokumen/detail";
    }

    // =============================================
    // CREATE - Tambah Dokumen
    // =============================================

    @GetMapping("/tambah")
    public String create(Model model, @AuthenticationPrincipal User currentUser) {
        DokumenRequestDTO dto = new DokumenRequestDTO();
        model.addAttribute("dokumenDTO", dto);
        model.addAttribute("dokumen", dto);
        model.addAttribute("dokumenId", null);
        addGroupedKategori(model);
        addCommonAttributes(model, currentUser, "dokumen-tambah");
        return "dokumen/form";
    }

    @PostMapping("/tambah")
    public String store(@Valid @ModelAttribute("dokumenDTO") DokumenRequestDTO dto,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("dokumenId", null);
            model.addAttribute("dokumen", dto);
            addGroupedKategori(model);
            addCommonAttributes(model, currentUser, "dokumen-tambah");
            return "dokumen/form";
        }
        try {
            dokumenService.simpanDokumen(dto, currentUser, file);
            redirectAttributes.addFlashAttribute("success", "Dokumen berhasil ditambahkan!");
            return "redirect:/dokumen";
        } catch (Exception e) {
            model.addAttribute("error", "Gagal menyimpan dokumen: " + e.getMessage());
            model.addAttribute("dokumenId", null);
            model.addAttribute("dokumen", dto);
            addGroupedKategori(model);
            addCommonAttributes(model, currentUser, "dokumen-tambah");
            return "dokumen/form";
        }
    }

    // =============================================
    // EDIT - Edit Dokumen
    // =============================================

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model,
            @AuthenticationPrincipal User currentUser) {
        Dokumen dokumen = dokumenService.findById(id);
        DokumenRequestDTO dto = new DokumenRequestDTO();
        dto.setNamaDokumen(dokumen.getNamaDokumen());
        dto.setTanggalMulai(dokumen.getTanggalMulai());
        dto.setTanggalBerakhir(dokumen.getTanggalBerakhir());
        dto.setStatus(dokumen.getStatus());
        if (dokumen.getSistemPengingat() != null)
            dto.setIntervalHari(dokumen.getSistemPengingat().getIntervalHari());
        if (dokumen.getKategori() != null)
            dto.setKategoriId(dokumen.getKategori().getKategoriId());

        model.addAttribute("dokumenDTO", dto);
        model.addAttribute("dokumen", dto);
        model.addAttribute("dokumenId", id);
        addGroupedKategori(model);
        addCommonAttributes(model, currentUser, "dokumen");
        return "dokumen/form-edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("dokumenDTO") DokumenRequestDTO dto,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes,
            Model model,
            @AuthenticationPrincipal User currentUser) {

        if (result.hasErrors()) {
            model.addAttribute("dokumenId", id);
            model.addAttribute("dokumen", dto);
            addGroupedKategori(model);
            addCommonAttributes(model, currentUser, "dokumen");
            return "dokumen/form-edit";
        }
        try {
            dokumenService.updateDokumen(id, dto, file);
            redirectAttributes.addFlashAttribute("success", "Dokumen berhasil diperbarui!");
            return "redirect:/dokumen";
        } catch (Exception e) {
            model.addAttribute("error", "Gagal update: " + e.getMessage());
            model.addAttribute("dokumenId", id);
            model.addAttribute("dokumen", dto);
            addGroupedKategori(model);
            addCommonAttributes(model, currentUser, "dokumen");
            return "dokumen/form-edit";
        }
    }

    // =============================================
    // DESTROY - Hapus Dokumen
    // =============================================

    @PostMapping("/hapus/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dokumenService.hapusDokumen(id);
            redirectAttributes.addFlashAttribute("success", "Dokumen berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal hapus: " + e.getMessage());
        }
        return "redirect:/dokumen";
    }

    // =============================================
    // PESERTA - Tambah & Hapus
    // =============================================

    @PostMapping("/{id}/peserta/tambah")
    public String tambahPeserta(@PathVariable Long id,
            @RequestParam("emailPeserta") String email,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {
        try {
            if (email == null || email.isBlank()) {
                redirectAttributes.addFlashAttribute("errorPeserta", "Email tidak boleh kosong.");
            } else {
                String cleanedEmail = email.trim();
                Dokumen dokumen = dokumenService.findById(id);
                dokumenService.tambahPeserta(id, cleanedEmail);
                String token = UUID.randomUUID().toString();
                emailInviteService.kirimUndanganPeserta(dokumen.getDokumenId(), currentUser,
                        List.of(cleanedEmail), List.of(token));
                redirectAttributes.addFlashAttribute("successPeserta",
                        "Peserta berhasil ditambahkan dan undangan dikirim ke: " + cleanedEmail);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorPeserta", "Gagal menambah peserta: " + e.getMessage());
        }
        return "redirect:/dokumen/" + id;
    }

    @PostMapping("/{id}/peserta/hapus")
    public String hapusPeserta(@PathVariable Long id,
            @RequestParam("emailPeserta") String email,
            RedirectAttributes redirectAttributes) {
        try {
            if (email == null || email.isBlank()) {
                redirectAttributes.addFlashAttribute("errorPeserta", "Email peserta tidak valid.");
            } else {
                dokumenService.hapusPeserta(id, email.trim());
                redirectAttributes.addFlashAttribute("successPeserta",
                        "Peserta " + email.trim() + " berhasil dihapus.");
            }
        } catch (Exception e) {
            log.error("Gagal menghapus peserta: ", e);
            redirectAttributes.addFlashAttribute("errorPeserta", "Gagal menghapus peserta: " + e.getMessage());
        }
        return "redirect:/dokumen/" + id;
    }

    // =============================================
    // UPLOAD FOTO → LLM EKSTRAK
    // =============================================

    @GetMapping("/upload-foto")
    public String uploadFotoForm(Model model, @AuthenticationPrincipal User currentUser) {
        addCommonAttributes(model, currentUser, "dokumen-upload");
        return "dokumen/upload-foto";
    }

    @PostMapping("/upload-foto")
    public String uploadFotoProses(@RequestParam("fotoFile") MultipartFile fotoFile,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (fotoFile.isEmpty()) {
            model.addAttribute("error", "Harap pilih file foto dokumen!");
            addCommonAttributes(model, currentUser, "dokumen-upload");
            return "dokumen/upload-foto";
        }
        try {
            log.info("Memulai ekstraksi LLM untuk file: {}", fotoFile.getOriginalFilename());
            DokumenExtractDTO extracted = geminiVisionService.ekstrakDariGambar(fotoFile);
            Dokumen saved = dokumenService.simpanDariEkstraksi(extracted, currentUser, fotoFile);
            redirectAttributes.addFlashAttribute("success",
                    "Dokumen berhasil diekstrak dan disimpan: " + saved.getNamaDokumen());
            return "redirect:/dokumen/" + saved.getDokumenId();
        } catch (Exception e) {
            log.error("Gagal proses upload foto: {}", e.getMessage());
            model.addAttribute("error", "Gagal memproses foto: " + e.getMessage());
            addCommonAttributes(model, currentUser, "dokumen-upload");
            return "dokumen/upload-foto";
        }
    }

    // =============================================
    // DOWNLOAD & PRATINJAU FILE
    // =============================================

    @GetMapping("/download/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> download(
            @PathVariable Long id) {
        try {
            Dokumen dokumen = dokumenService.findById(id);
            if (dokumen.getFilePath() == null)
                return org.springframework.http.ResponseEntity.notFound().build();

            java.nio.file.Path path = java.nio.file.Paths.get(dokumen.getFilePath());
            org.springframework.core.io.Resource resource =
                    new org.springframework.core.io.UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("File tidak dapat dibaca: {}", dokumen.getFilePath());
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            String contentType;
            try {
                contentType = java.nio.file.Files.probeContentType(path);
            } catch (java.io.IOException ex) {
                contentType = null;
            }
            if (contentType == null) contentType = "application/octet-stream";

            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Gagal download file id: {}", id, e);
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}