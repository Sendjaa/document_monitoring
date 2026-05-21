package com.docmonitor.controller;

import com.docmonitor.model.TipeKategori;
import com.docmonitor.model.User;
import com.docmonitor.scheduler.DokumenReminderScheduler;
import com.docmonitor.service.DokumenService;
import com.docmonitor.service.KategoriDokumenService;
import com.docmonitor.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final DokumenService dokumenService;
    private final KategoriDokumenService kategoriService;
    private final DokumenReminderScheduler scheduler;

    // Constructor
    public AdminController(UserService userService, DokumenService dokumenService,
            KategoriDokumenService kategoriService, DokumenReminderScheduler scheduler) {
        this.userService = userService;
        this.dokumenService = dokumenService;
        this.kategoriService = kategoriService;
        this.scheduler = scheduler;
    }

    private void addCommonAttributes(Model model, User currentUser) {
        model.addAttribute("currentUser", currentUser);
    }
    @GetMapping
    public String adminPanel(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalDokumen", dokumenService.findAll().size());
        model.addAttribute("totalKategori", kategoriService.findAll().size());
        model.addAttribute("activeMenu", "admin");
        addCommonAttributes(model, currentUser);
        return "admin/panel";
    }

    @GetMapping("/users")
    public String listUsers(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("userList", userService.findAll());
        model.addAttribute("activeMenu", "admin-users");
        addCommonAttributes(model, currentUser);
        return "admin/users";
    }

    @GetMapping("/kategori")
    public String listKategori(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("kategoriList", kategoriService.findAll());
        model.addAttribute("activeMenu", "kategori");
        addCommonAttributes(model, currentUser);
        return "admin/kategori";
    }

    @PostMapping("/kategori/tambah")
    public String tambahKategori(@RequestParam String namaKategori,
            @RequestParam(required = false) String deskripsi,
            @RequestParam(required = false, defaultValue = "INDIVIDU") String tipe,
            RedirectAttributes redirectAttributes) {
        try {
            TipeKategori tipeKategori = TipeKategori.valueOf(tipe.toUpperCase());
            kategoriService.simpan(namaKategori, deskripsi, tipeKategori);
            redirectAttributes.addFlashAttribute("success", "Kategori berhasil ditambahkan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kategori";
    }

    @PostMapping("/kategori/hapus/{id}")
    public String hapusKategori(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            kategoriService.hapus(id);
            redirectAttributes.addFlashAttribute("success", "Kategori berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal hapus: " + e.getMessage());
        }
        return "redirect:/admin/kategori";
    }

    /**
     * Trigger cron job secara manual oleh admin.
     */
    @PostMapping("/scheduler/run")
    public String runSchedulerManual(RedirectAttributes redirectAttributes) {
        try {
            scheduler.jalankanManual();
            redirectAttributes.addFlashAttribute("success",
                    "✅ Pengecekan pengingat berhasil dijalankan! Cek log untuk detailnya.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menjalankan scheduler: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deactivateUser(id);
            redirectAttributes.addFlashAttribute("success", "User berhasil dinonaktifkan.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}