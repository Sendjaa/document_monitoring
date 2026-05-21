package com.docmonitor.controller;

import com.docmonitor.dto.DokumenExtractDTO;
import com.docmonitor.dto.DokumenRequestDTO;
import com.docmonitor.model.Dokumen;
import com.docmonitor.model.KategoriDokumen;
import com.docmonitor.model.StatusDokumen;
import com.docmonitor.model.TipeKategori;
import com.docmonitor.model.User;
import com.docmonitor.service.DokumenService;
import com.docmonitor.service.KategoriDokumenService;
import com.docmonitor.service.UserService;
import com.docmonitor.service.EmailInviteService;
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

@Controller
@RequestMapping("/dokumen")
public class DokumenController {

    private static final Logger log = LoggerFactory.getLogger(DokumenController.class);

    private final DokumenService dokumenService;
    private final KategoriDokumenService kategoriService;
    private final UserService userService;
    private final EmailInviteService emailInviteService;

    // Constructor
    public DokumenController(DokumenService dokumenService, KategoriDokumenService kategoriService, UserService userService, EmailInviteService emailInviteService) {
        this.dokumenService = dokumenService;
        this.kategoriService = kategoriService;
        this.userService = userService;
        this.emailInviteService = emailInviteService;
    }

    // =============================================
    // Helper: add grouped categories to model
    // =============================================

    private void addCommonAttributes(Model model, User currentUser, String activeMenu) {
        model.addAttribute("currentUser", currentUser);
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("activeMenu", activeMenu);

        // Inisialisasi default agar tidak null saat diparsing Thymeleaf
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

        // Group utama: INDIVIDU (Pribadi) / BERSAMA
        Map<String, List<KategoriDokumen>> groupedKategori = new LinkedHashMap<>();
        for (TipeKategori tipe : TipeKategori.values()) {
            groupedKategori.put(tipe.getDisplayName(), new ArrayList<>());
        }
        for (KategoriDokumen k : allKategori) {
            if (k.getTipe() != null) {
                String key = k.getTipe().getDisplayName();
                groupedKategori.computeIfAbsent(key, k2 -> new ArrayList<>()).add(k);
            }
        }
        
        List<String> subGroupPribadi = java.util.Arrays.asList(
            "Identitas", "Pendidikan", "Legalitas", "Kendaraan", "Keuangan");
        Map<String, List<KategoriDokumen>> subGroupedPribadi = new LinkedHashMap<>();
        for (String sg : subGroupPribadi) {
            subGroupedPribadi.put(sg, new ArrayList<>());
        }
        List<KategoriDokumen> individu = groupedKategori.getOrDefault("Individu", new ArrayList<>());
        for (KategoriDokumen k : individu) {
            boolean matched = false;
            for (String sg : subGroupPribadi) {
                if (k.getNamaKategori().startsWith(sg + " - ") || k.getNamaKategori().equalsIgnoreCase(sg)) {
                    subGroupedPribadi.get(sg).add(k);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                subGroupedPribadi.get("Identitas").add(k);
            }
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
    // LIST
    // =============================================

    @GetMapping
    public String listDokumen(@AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {
        List<Dokumen> dokumenList;
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (keyword != null && !keyword.isBlank()) {
            dokumenList = dokumenService.searchDokumen(keyword, currentUser.getUserId());
        } else if (isAdmin) {
            dokumenList = dokumenService.findAll();
        } else {
            dokumenList = dokumenService.findByUser(currentUser.getUserId());
        }

        model.addAttribute("dokumenList", dokumenList);
        model.addAttribute("keyword", keyword);
        addCommonAttributes(model, currentUser, "dokumen");
        return "dokumen/list";
    }

    // =============================================
    // DETAIL
    // =============================================

    @GetMapping("/{id}")
    public String detailDokumen(@PathVariable Long id, Model model,
            @AuthenticationPrincipal User currentUser) {
        Dokumen dokumen = dokumenService.findById(id);
        model.addAttribute("dokumen", dokumen);
        model.addAttribute("pesertaList", dokumenService.getPesertaByDokumen(id));
        boolean isBersama = dokumen.getKategori() != null
            && TipeKategori.BERSAMA.equals(dokumen.getKategori().getTipe());
        model.addAttribute("isBersama", isBersama);
        addCommonAttributes(model, currentUser, "dokumen");
        return "dokumen/detail";
    }

    // =============================================
    // PESERTA – Tambah & Hapus dari halaman Detail
    // =============================================

    @PostMapping("/{id}/peserta/tambah")
    public String tambahPesertaDariDetail(@PathVariable Long id,
            @RequestParam("emailPeserta") String email,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {
        try {
            if (email == null || email.isBlank()) {
                redirectAttributes.addFlashAttribute("errorPeserta", "Email tidak boleh kosong.");
            } else {
                String cleanedEmail = email.trim();
                
                Dokumen dokumen = dokumenService.findById(id);
                
                // 2. Jalankan logic database bawaan kamu
                dokumenService.tambahPeserta(id, cleanedEmail);
                
                // 3. Pemicu kirim email undangan (berjalan secara Async/Background thread)
                List<String> penerimaEmail = List.of(cleanedEmail);
                emailInviteService.kirimUndanganPeserta(dokumen, currentUser, penerimaEmail);
                
                redirectAttributes.addFlashAttribute("successPeserta", "Peserta berhasil ditambahkan dan undangan email telah dikirim ke: " + cleanedEmail);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorPeserta", "Gagal menambah peserta: " + e.getMessage());
        }
        return "redirect:/dokumen/" + id;
    }

    @PostMapping("/{id}/peserta/hapus")
    public String hapusPesertaDariDetail(@PathVariable Long id,
            @RequestParam("emailPeserta") String email, // Pastikan name di HTML adalah emailPeserta
            RedirectAttributes redirectAttributes) {
        try {
            if (email == null || email.isBlank()) {
                redirectAttributes.addFlashAttribute("errorPeserta", "Email peserta tidak valid.");
            } else {
                String cleanedEmail = email.trim();
                
                // 1. Jalankan fungsi hapus dari database lewat service kamu
                dokumenService.hapusPeserta(id, cleanedEmail);
                
                // 2. Berikan notifikasi sukses ke halaman detail
                redirectAttributes.addFlashAttribute("successPeserta", "Peserta dengan email " + cleanedEmail + " berhasil dihapus dari dokumen ini.");
            }
        } catch (Exception e) {
            log.error("Gagal menghapus peserta: ", e);
            redirectAttributes.addFlashAttribute("errorPeserta", "Gagal menghapus peserta: " + e.getMessage());
        }
        return "redirect:/dokumen/" + id;
    }

    // =============================================
    // TAMBAH MANUAL
    // =============================================

    @GetMapping("/tambah")
    public String formTambah(Model model, @AuthenticationPrincipal User currentUser) {
        // Pastikan objek DTO diinisialisasi dengan benar
        DokumenRequestDTO dto = new DokumenRequestDTO();
        model.addAttribute("dokumenDTO", dto);
        model.addAttribute("dokumen", dto); 
        model.addAttribute("dokumenId", null); 
        addGroupedKategori(model);
        addCommonAttributes(model, currentUser, "dokumen-tambah");
        return "dokumen/form";
    }

    @PostMapping("/tambah")
    public String prosesTambah(@Valid @ModelAttribute("dokumenDTO") DokumenRequestDTO dto,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("dokumenId", null);
            model.addAttribute("dokumen", dto); // Sediakan alias agar template tidak crash
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
            model.addAttribute("dokumen", dto); // Sediakan alias agar template tidak crash
            addGroupedKategori(model);
            addCommonAttributes(model, currentUser, "dokumen-tambah");
            return "dokumen/form";
        }
    }

    // =============================================
    // UPLOAD FOTO → LLM EKSTRAK
    // =============================================

    @GetMapping("/upload-foto")
    public String formUploadFoto(Model model, @AuthenticationPrincipal User currentUser) {
        addCommonAttributes(model, currentUser, "dokumen-upload");
        return "dokumen/upload-foto";
    }

    @PostMapping("/upload-foto")
    public String prosesUploadFoto(@RequestParam("fotoFile") MultipartFile fotoFile,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (fotoFile.isEmpty()) {
            model.addAttribute("error", "Harap pilih file foto dokumen!");
            addCommonAttributes(model, currentUser, "dokumen-upload");
            return "dokumen/upload-foto";
        }
        try {
            // Ekstrak data menggunakan Gemini Vision
            log.info("Memulai ekstraksi LLM untuk file: {}", fotoFile.getOriginalFilename());
            DokumenExtractDTO extracted = dokumenService.ekstrakDariGambar(fotoFile);

            // Simpan ke database
            Dokumen saved = dokumenService.simpanDariEkstraksi(extracted, currentUser, fotoFile);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Dokumen berhasil diekstrak dan disimpan! Nama: " + saved.getNamaDokumen());
            return "redirect:/dokumen/" + saved.getDokumenId();

        } catch (Exception e) {
            log.error("Gagal proses upload foto: {}", e.getMessage());
            model.addAttribute("error", "Gagal memproses foto: " + e.getMessage());
            addCommonAttributes(model, currentUser, "dokumen-upload");
            return "dokumen/upload-foto";
        }
    }

    // =============================================
    // EDIT
    // =============================================

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model,
            @AuthenticationPrincipal User currentUser) {
        Dokumen dokumen = dokumenService.findById(id);
        DokumenRequestDTO dto = new DokumenRequestDTO();
        dto.setNamaDokumen(dokumen.getNamaDokumen());
        dto.setTanggalMulai(dokumen.getTanggalMulai());
        dto.setTanggalBerakhir(dokumen.getTanggalBerakhir());
        dto.setStatus(dokumen.getStatus());
        if (dokumen.getSistemPengingat() != null) dto.setIntervalHari(dokumen.getSistemPengingat().getIntervalHari());
        if (dokumen.getKategori() != null) {
            dto.setKategoriId(dokumen.getKategori().getKategoriId());
        }

        model.addAttribute("dokumenDTO", dto);
        model.addAttribute("dokumen", dto); // Gunakan alias yang sama dengan formTambah
        model.addAttribute("dokumenId", id);
        addGroupedKategori(model);
        addCommonAttributes(model, currentUser, "dokumen");
        return "dokumen/form-edit";
    }

    @PostMapping("/edit/{id}")
    public String prosesEdit(@PathVariable Long id,
            @Valid @ModelAttribute("dokumenDTO") DokumenRequestDTO dto,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes,
            Model model,
            @AuthenticationPrincipal User currentUser) {
        if (result.hasErrors()) {
            model.addAttribute("dokumenId", id);
            model.addAttribute("dokumen", dto); // Tambahkan alias agar template tidak crash
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
            model.addAttribute("dokumen", dto); // Tambahkan alias agar template tidak crash
            addGroupedKategori(model);
            addCommonAttributes(model, currentUser, "dokumen");
            return "dokumen/form-edit";
        }
    }

    // =============================================
    // HAPUS
    // =============================================

    @PostMapping("/hapus/{id}")
    public String hapusDokumen(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            dokumenService.hapusDokumen(id);
            redirectAttributes.addFlashAttribute("success", "Dokumen berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal hapus: " + e.getMessage());
        }
        return "redirect:/dokumen";
    }
}