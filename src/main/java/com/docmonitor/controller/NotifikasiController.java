package com.docmonitor.controller;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.User;
import com.docmonitor.service.DokumenService;
import com.docmonitor.model.StatusDokumen;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/notifikasi")
public class NotifikasiController {

    private final DokumenService dokumenService;

    public NotifikasiController(DokumenService dokumenService) {
        this.dokumenService = dokumenService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal User currentUser, Model model) {
        List<Dokumen> semua = getDokumenForUser(currentUser);

        List<Map<String, Object>> notifList = new ArrayList<>();
        for (Dokumen doc : semua) {
            if (doc.getStatus() == null) continue;
            if (doc.getStatus() == StatusDokumen.KADALUARSA) {
                Map<String, Object> notif = new LinkedHashMap<>();
                notif.put("dokumenId", doc.getDokumenId());
                notif.put("namaDokumen", doc.getNamaDokumen());
                notif.put("icon", "bi-exclamation-triangle-fill");
                notif.put("iconClass", "urgent");
                notif.put("title", "Dokumen Kadaluarsa");
                notif.put("message", "Dokumen '" + doc.getNamaDokumen() + "' telah kadaluarsa pada " + doc.getTanggalBerakhir());
                notif.put("tanggalBerakhir", doc.getTanggalBerakhir());
                notif.put("sisaHari", doc.getSisaHari());
                notifList.add(notif);
            } else if (doc.getStatus() == StatusDokumen.AKAN_HABIS) {
                Map<String, Object> notif = new LinkedHashMap<>();
                notif.put("dokumenId", doc.getDokumenId());
                notif.put("namaDokumen", doc.getNamaDokumen());
                notif.put("icon", "bi-clock-fill");
                notif.put("iconClass", "warning");
                notif.put("title", "Dokumen Akan Berakhir");
                notif.put("message", "Dokumen '" + doc.getNamaDokumen() + "' akan berakhir dalam " + doc.getSisaHari() + " hari");
                notif.put("tanggalBerakhir", doc.getTanggalBerakhir());
                notif.put("sisaHari", doc.getSisaHari());
                notifList.add(notif);
            }
        }

        model.addAttribute("notifList", notifList);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeMenu", "notifikasi");
        return "notifikasi/index";
    }

    private List<Dokumen> getDokumenForUser(User currentUser) {
        if (currentUser.getRole() != null && currentUser.getRole().equals("ADMIN")) {
            return dokumenService.findAll();
        }
        List<Dokumen> result = new ArrayList<>(dokumenService.findByUser(currentUser.getUserId()));
        List<Dokumen> kolaborasi = dokumenService.findDokumenKolaborasi(currentUser.getUserId());
        for (Dokumen k : kolaborasi) {
            if (result.stream().noneMatch(d -> d.getDokumenId().equals(k.getDokumenId()))) {
                result.add(k);
            }
        }
        return result;
    }
}