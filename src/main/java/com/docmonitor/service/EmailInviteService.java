package com.docmonitor.service;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.User;
import com.docmonitor.repository.DokumenRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class EmailInviteService {

    private static final Logger log = LoggerFactory.getLogger(EmailInviteService.class);
    private final DokumenRepository dokumenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public EmailInviteService(DokumenRepository dokumenRepository, JavaMailSender mailSender) {
        this.dokumenRepository = dokumenRepository;
        this.mailSender = mailSender;
    }

    @Async
    public void kirimUndanganPeserta(Long dokumenId, User pengirim, List<String> emailPeserta, List<String> inviteTokens) {
        if (emailPeserta == null || emailPeserta.isEmpty()) return;

        // Ambil data fresh dari database di dalam thread ini untuk menghindari error "no Session"
        Dokumen dokumen = dokumenRepository.findById(dokumenId)
                .orElseThrow(() -> new RuntimeException("Dokumen tidak ditemukan untuk ID: " + dokumenId));

        log.info("Memulai pengiriman {} email undangan di background thread: {}", emailPeserta.size(), Thread.currentThread().getName());
        for (int i = 0; i < emailPeserta.size(); i++) {
            String email = emailPeserta.get(i);
            String token = inviteTokens.get(i);

            if (email != null && !email.isBlank()) {
                try {
                    kirimSatuUndangan(dokumen, pengirim, email.trim(), token);
                    log.info("Undangan berhasil dikirim ke {} untuk dokumen '{}'", email, dokumen.getNamaDokumen());
                } catch (Exception e) {
                    log.error("Gagal kirim undangan ke {}: {}", email, e.getMessage());
                }
            }
        }
    }

    private void kirimSatuUndangan(Dokumen dokumen, User pengirim, String toEmail, String inviteToken)
            throws MessagingException, UnsupportedEncodingException {

        String subject = "📄 Anda diundang ke dokumen bersama: " + dokumen.getNamaDokumen();
        String dokumenUrl = baseUrl;
        String html = buildInviteHtml(dokumen, pengirim, toEmail, dokumenUrl, inviteToken);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, fromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String buildInviteHtml(Dokumen dokumen, User pengirim, String toEmail, String dokumenUrl, String inviteToken) {
        String kategori = dokumen.getKategori() != null ? dokumen.getKategori().getNamaKategori() : "Tidak dikategorikan";
        String tglMulai = dokumen.getTanggalMulai() != null ? dokumen.getTanggalMulai().toString() : "-";
        String tglBerakhir = dokumen.getTanggalBerakhir() != null ? dokumen.getTanggalBerakhir().toString() : "-";

        return "<!DOCTYPE html><html lang='id'><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f0f4f8;font-family:\"Segoe UI\",Arial,sans-serif;'>"
            + "<div style='max-width:580px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1);'>"
            + "<div style='background:linear-gradient(135deg,#0f4c81,#1a6db5);padding:32px 36px;text-align:center;'>"
            + "<div style='width:56px;height:56px;background:linear-gradient(135deg,#f59e0b,#ff6b35);border-radius:14px;display:inline-flex;align-items:center;justify-content:center;font-size:26px;margin-bottom:14px;'>📄</div>"
            + "<h1 style='color:#fff;font-size:22px;font-weight:800;margin:0;'>Undangan Dokumen Bersama</h1></div>"
            + "<div style='padding:32px 36px;'><p style='font-size:15px;color:#374151;margin-bottom:20px;'>Halo,</p>"
            + "<p style='font-size:14px;color:#374151;line-height:1.6;margin-bottom:20px;'>"
            + "<strong style='color:#0f4c81;'>" + escHtml(pengirim.getName()) + "</strong> mengundang Anda sebagai <strong>peserta</strong> dalam dokumen bersama berikut:</p>"
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:20px;margin-bottom:24px;'>"
            + "<div style='font-size:16px;font-weight:700;color:#0f172a;'>" + escHtml(dokumen.getNamaDokumen()) + "</div>"
            + "<table style='width:100%;border-collapse:collapse;'>"
            + tableRow("Kategori", kategori, "#f1f5f9")
            + tableRow("Tanggal Mulai", tglMulai, "#fff")
            + tableRow("Tanggal Berakhir", tglBerakhir, "#f1f5f9")
            + "</table></div>"
            + "<div style='text-align:center;margin-bottom:24px;'><a href='" + dokumenUrl + "/auth/login' style='display:inline-block;padding:14px 32px;background:linear-gradient(135deg,#0f4c81,#1a6db5);color:#fff;text-decoration:none;border-radius:10px;font-size:15px;font-weight:700;'>Lihat Dokumen →</a></div>"
            + "<div style='background:#fef3c7;border-left:4px solid #f59e0b;padding:14px 16px;border-radius:0 8px 8px 0;margin-bottom:24px;'>"
            + "<p style='font-size:13px;color:#92400e;margin:0;'>⚠️ Jika Anda belum memiliki akun, <a href='" + baseUrl + "/auth/register?token=" + inviteToken + "' style='color:#0f4c81;font-weight:600;'>daftar di sini</a>.</p></div>"
            + "</div></div></body></html>";
    }

    private String tableRow(String label, String value, String bg) {
        return "<tr style='background:" + bg + ";'><td style='padding:9px 12px;border:1px solid #e2e8f0;font-size:13px;font-weight:600;color:#374151;width:40%;'>" + label + "</td><td style='padding:9px 12px;border:1px solid #e2e8f0;font-size:13px;color:#0f172a;'>" + escHtml(value) + "</td></tr>";
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    @Async
    public void kirimNotifikasiKePeserta(Dokumen dokumen, String emailPeserta) {
        try {
            long sisaHari = dokumen.getSisaHari();
            String subject = (sisaHari < 0) ? "⚠️ [KADALUARSA] Dokumen: " + dokumen.getNamaDokumen() : "📋 [PENGINGAT] Dokumen berakhir dalam " + sisaHari + " hari: " + dokumen.getNamaDokumen();
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailPeserta);
            helper.setSubject(subject);
            helper.setText(buildReminderHtml(dokumen, emailPeserta, "Peserta"), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Gagal kirim notifikasi: {}", e.getMessage());
        }
    }

   public String buildReminderHtml(Dokumen dokumen, String namaPenerima, String peran) {
    String namaDokumen = dokumen.getNamaDokumen() != null ? dokumen.getNamaDokumen() : "Dokumen Tanpa Nama";
    String tenggatWaktu = dokumen.getTanggalBerakhir() != null ? dokumen.getTanggalBerakhir().toString() : "-";
    String urlDokumen = baseUrl + "/dokumen/" + dokumen.getDokumenId();

    return "<!DOCTYPE html>" +
           "<html>" +
           "<head>" +
           "    <meta charset='UTF-8'>" +
           "    <style>" +
           "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333333; line-height: 1.6; background-color: #f9f9f9; margin: 0; padding: 0; }" +
           "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border-top: 5px solid #4F46E5; }" +
           "        .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eeeeee; }" +
           "        .header h2 { color: #4F46E5; margin: 0; font-size: 24px; }" +
           "        .content { padding: 20px 0; }" +
           "        .greeting { font-size: 16px; font-weight: bold; color: #111111; }" +
           "        .details-box { background-color: #F3F4F6; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #4F46E5; }" +
           "        .details-item { margin-bottom: 10px; font-size: 14px; }" +
           "        .details-label { font-weight: bold; color: #4B5563; display: inline-block; width: 120px; }" +
           "        .cta-container { text-align: center; margin: 30px 0 10px 0; }" +
           "        .btn { background-color: #4F46E5; color: #ffffff !important; text-decoration: none; padding: 12px 24px; font-weight: bold; border-radius: 6px; display: inline-block; transition: background-color 0.2s; }" +
           "        .btn:hover { background-color: #4338CA; }" +
           "        .footer { text-align: center; font-size: 12px; color: #9CA3AF; margin-top: 30px; border-top: 1px solid #eeeeee; padding-top: 15px; }" +
           "    </style>" +
           "</head>" +
           "<body>" +
           "    <div class='container'>" +
           "        <div class='header'>" +
           "            <h2>Pengingat Dokumen</h2>" +
           "        </div>" +
           "        <div class='content'>" +
           "            <p class='greeting'>Halo, " + namaPenerima + "</p>" +
           "            <p>Ini adalah pengingat otomatis bahwa dokumen berikut memerlukan perhatian atau tindakan Anda sebagai <strong>" + peran + "</strong>.</p>" +
           "            " +
           "            <div class='details-box'>" +
           "                <div class='details-item'><span class='details-label'>Nama Dokumen:</span> " + namaDokumen + "</div>" +
           "                <div class='details-item'><span class='details-label'>Tenggat Waktu:</span> " + tenggatWaktu + "</div>" +
           "                <div class='details-item'><span class='details-label'>Status:</span> <span style='color: #D97706; font-weight: bold;'>" + dokumen.getStatus() + "</span></div>" +
           "            </div>" +
           "            " +
           "            <div class='cta-container'>" +
           "                <a href='" + urlDokumen + "' class='btn'>Buka Dokumen</a>" +
           "            </div>" +
           "        </div>" +
           "        <div class='footer'>" +
           "            <p>Email ini dikirim secara otomatis oleh Sistem Monitoring Dokumen.<br>Mohon tidak membalas email ini.</p>" +
           "        </div>" +
           "    </div>" +
           "</body>" +
           "</html>";
}
}