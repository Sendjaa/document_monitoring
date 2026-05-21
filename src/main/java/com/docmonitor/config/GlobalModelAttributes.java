package com.docmonitor.config;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.User;
import com.docmonitor.service.DokumenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.*;

@ControllerAdvice
public class GlobalModelAttributes {

    private final DokumenService dokumenService;

    public GlobalModelAttributes(DokumenService dokumenService) {
        this.dokumenService = dokumenService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal User user) {
        return user;
    }

    @ModelAttribute("todayFormatted")
    public String todayFormatted() {
        return java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
                        new java.util.Locale("id", "ID")));
    }

    @ModelAttribute("activeMenu")
    public String activeMenu(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/users")) {
            return "admin-users";
        }
        if (uri.startsWith("/admin/kategori")) {
            return "kategori";
        }
        if (uri.startsWith("/admin")) {
            return "admin";
        }
        if (uri.startsWith("/dokumen/upload-foto")) {
            return "upload-foto";
        }
        if (uri.startsWith("/dokumen/tambah")) {
            return "tambah";
        }
        if (uri.startsWith("/dokumen")) {
            return "dokumen";
        }
        if (uri.startsWith("/dashboard")) {
            return "dashboard";
        }
        return "";
    }

    @ModelAttribute("notifications")
    public List<Map<String, Object>> notifications(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return Collections.emptyList();

        List<Dokumen> userDocs;
        if (currentUser.getRole() != null && currentUser.getRole().equals("ADMIN")) {
            userDocs = dokumenService.findAll();
        } else {
            userDocs = dokumenService.findByUser(currentUser.getUserId());
        }

        List<Map<String, Object>> notifications = new ArrayList<>();
        for (Dokumen doc : userDocs) {
            if (doc.getStatus() == null) continue;

            Map<String, Object> notif = new LinkedHashMap<>();
            notif.put("dokumenId", doc.getDokumenId());
            notif.put("namaDokumen", doc.getNamaDokumen());

            switch (doc.getStatus()) {
                case KADALUARSA:
                    notif.put("icon", "bi-exclamation-triangle");
                    notif.put("iconClass", "urgent");
                    notif.put("title", "Dokumen Kadaluarsa");
                    notif.put("message", "Dokumen '" + doc.getNamaDokumen() + "' telah kadaluarsa pada " + doc.getTanggalBerakhir());
                    notif.put("time", "Segera");
                    notifications.add(notif);
                    break;
                case AKAN_HABIS:
                    notif.put("icon", "bi-clock");
                    notif.put("iconClass", "warning");
                    notif.put("title", "Dokumen Akan Berakhir");
                    notif.put("message", "Dokumen '" + doc.getNamaDokumen() + "' akan berakhir dalam " + doc.getSisaHari() + " hari");
                    notif.put("time", doc.getSisaHari() + " hari lagi");
                    notifications.add(notif);
                    break;
                default:
                    break;
            }
        }
        return notifications;
    }

    @ModelAttribute("notifCount")
    public int notifCount(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return 0;

        List<Dokumen> userDocs;
        if (currentUser.getRole() != null && currentUser.getRole().equals("ADMIN")) {
            userDocs = dokumenService.findAll();
        } else {
            userDocs = dokumenService.findByUser(currentUser.getUserId());
        }

        return (int) userDocs.stream()
                .filter(d -> d.getStatus() != null)
                .filter(d -> d.getStatus() == com.docmonitor.model.StatusDokumen.KADALUARSA
                        || d.getStatus() == com.docmonitor.model.StatusDokumen.AKAN_HABIS)
                .count();
    }
}
