package com.docmonitor.controller;

import com.docmonitor.model.StatusDokumen;
import com.docmonitor.model.User;
import com.docmonitor.service.DokumenService;
import com.docmonitor.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DokumenService dokumenService;

    // Constructor
    public DashboardController(DokumenService dokumenService) {
        this.dokumenService = dokumenService;
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
            model.addAttribute("recentDokumen", dokumenService.findAll().stream().limit(5).toList());
        } else {
            // User biasa: lihat dokumen milik sendiri
            Long userId = currentUser.getUserId();
            model.addAttribute("totalDokumen", dokumenService.countByUser(userId));
            model.addAttribute("dokumenAktif",
                dokumenService.findByUser(userId).stream().filter(d -> d.getStatus() == StatusDokumen.AKTIF).count());
            model.addAttribute("dokumenAkanHabis",
                dokumenService.findByUser(userId).stream().filter(d -> d.getStatus() == StatusDokumen.AKAN_HABIS).count());
            model.addAttribute("dokumenKadaluarsa",
                dokumenService.findByUser(userId).stream().filter(d -> d.getStatus() == StatusDokumen.KADALUARSA).count());
            model.addAttribute("recentDokumen",
                dokumenService.findByUser(userId).stream().limit(5).toList());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);
        return "dashboard";
    }
}
