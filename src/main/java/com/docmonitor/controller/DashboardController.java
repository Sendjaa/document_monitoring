package com.docmonitor.controller;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.StatusDokumen;
import com.docmonitor.model.User;
import com.docmonitor.service.DokumenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final DokumenService dokumenService;

    public DashboardController(DokumenService dokumenService) {
        this.dokumenService = dokumenService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logoutRedirect() {
        return "redirect:/auth/logout";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User currentUser, Model model) {
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        if (isAdmin) {
            // Admin: lihat semua dokumen
            model.addAttribute("totalDokumen", dokumenService.findAll().size());
            model.addAttribute("dokumenAktif", dokumenService.countByStatus(StatusDokumen.AKTIF));
            model.addAttribute("dokumenAkanHabis", dokumenService.countByStatus(StatusDokumen.AKAN_HABIS));
            model.addAttribute("dokumenKadaluarsa", dokumenService.countByStatus(StatusDokumen.KADALUARSA));
            model.addAttribute("recentDokumen", dokumenService.findAll().stream()
                    .sorted(Comparator.comparing(Dokumen::getDokumenId).reversed())
                    .limit(5).toList());
        } else {
            // User biasa: lihat dokumen milik sendiri DAN dokumen kolaborasi
            Long userId = currentUser.getUserId();
            
            List<Dokumen> milikSendiri = dokumenService.findByUser(userId);
            List<Dokumen> kolaborasi = dokumenService.findDokumenKolaborasi(userId);
            
            // Gabungkan keduanya
            List<Dokumen> semuaDokumen = new ArrayList<>(milikSendiri);
            semuaDokumen.addAll(kolaborasi);

            // Statistik berdasarkan dokumen gabungan
            model.addAttribute("totalDokumen", semuaDokumen.size());
            model.addAttribute("dokumenAktif", 
                semuaDokumen.stream().filter(d -> d.getStatus() == StatusDokumen.AKTIF).count());
            model.addAttribute("dokumenAkanHabis", 
                semuaDokumen.stream().filter(d -> d.getStatus() == StatusDokumen.AKAN_HABIS).count());
            model.addAttribute("dokumenKadaluarsa", 
                semuaDokumen.stream().filter(d -> d.getStatus() == StatusDokumen.KADALUARSA).count());
            
            // Recent dokumen dari gabungan, diurutkan dari yang terbaru
            model.addAttribute("recentDokumen", semuaDokumen.stream()
                    .sorted(Comparator.comparing(Dokumen::getDokumenId).reversed())
                    .limit(5).toList());
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("activeMenu", "dashboard");
        return "dashboard";
    }
}