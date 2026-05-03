package com.docmonitor.controller;

import com.docmonitor.dto.DokumenExtractDTO;
import com.docmonitor.dto.DokumenRequestDTO;
import com.docmonitor.model.Dokumen;
import com.docmonitor.model.User;
import com.docmonitor.service.DokumenService;
import com.docmonitor.service.KategoriDokumenService;
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

import java.util.List;

@Controller
@RequestMapping("/dokumen")
public class DokumenController {

    private static final Logger log = LoggerFactory.getLogger(DokumenController.class);

    private final DokumenService dokumenService;
    private final KategoriDokumenService kategoriService;

    // Constructor
    public DokumenController(DokumenService dokumenService, KategoriDokumenService kategoriService) {
        this.dokumenService = dokumenService;
        this.kategoriService = kategoriService;
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
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        if (keyword != null && !keyword.isBlank()) {
            dokumenList = dokumenService.searchDokumen(keyword, currentUser.getUserId());
        } else if (isAdmin) {
            dokumenList = dokumenService.findAll();
        } else {
            dokumenList = dokumenService.findByUser(currentUser.getUserId());
        }

        model.addAttribute("dokumenList", dokumenList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);
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
        model.addAttribute("currentUser", currentUser);
        return "dokumen/detail";
    }

    // =============================================
    // TAMBAH MANUAL
    // =============================================

    @GetMapping("/tambah")
    public String formTambah(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("dokumenDTO", new DokumenRequestDTO());
        model.addAttribute("kategoriList", kategoriService.findAll());
        model.addAttribute("currentUser", currentUser);
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
            model.addAttribute("kategoriList", kategoriService.findAll());
            return "dokumen/form";
        }
        try {
            dokumenService.simpanDokumen(dto, currentUser, file);
            redirectAttributes.addFlashAttribute("success", "Dokumen berhasil ditambahkan!");
            return "redirect:/dokumen";
        } catch (Exception e) {
            model.addAttribute("error", "Gagal menyimpan dokumen: " + e.getMessage());
            model.addAttribute("kategoriList", kategoriService.findAll());
            return "dokumen/form";
        }
    }

    // =============================================
    // UPLOAD FOTO → LLM EKSTRAK
    // =============================================

    @GetMapping("/upload-foto")
    public String formUploadFoto(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("currentUser", currentUser);
        return "dokumen/upload-foto";
    }

    @PostMapping("/upload-foto")
    public String prosesUploadFoto(@RequestParam("fotoFile") MultipartFile fotoFile,
                                   @AuthenticationPrincipal User currentUser,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (fotoFile.isEmpty()) {
            model.addAttribute("error", "Harap pilih file foto dokumen!");
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
        if (dokumen.getKategori() != null) {
            dto.setKategoriId(dokumen.getKategori().getKategoriId());
        }

        model.addAttribute("dokumenDTO", dto);
        model.addAttribute("dokumenId", id);
        model.addAttribute("kategoriList", kategoriService.findAll());
        model.addAttribute("currentUser", currentUser);
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
            model.addAttribute("kategoriList", kategoriService.findAll());
            return "dokumen/form-edit";
        }
        try {
            dokumenService.updateDokumen(id, dto, file);
            redirectAttributes.addFlashAttribute("success", "Dokumen berhasil diperbarui!");
            return "redirect:/dokumen";
        } catch (Exception e) {
            model.addAttribute("error", "Gagal update: " + e.getMessage());
            model.addAttribute("kategoriList", kategoriService.findAll());
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
