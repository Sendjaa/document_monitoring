package com.docmonitor.service;

import com.docmonitor.model.Dokumen;
import com.docmonitor.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async; // Tambahkan import ini
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class EmailInviteService {

    private static final Logger log = LoggerFactory.getLogger(EmailInviteService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public EmailInviteService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Kirim email undangan ke semua peserta dokumen bersama.
     * Ditambahkan @Async agar proses looping email berjalan di background thread
     * dan tidak memblokir response HTTP user di Controller.
     */
    @Async
    public void kirimUndanganPeserta(Dokumen dokumen, User pengirim, List<String> emailPeserta) {
        if (emailPeserta == null || emailPeserta.isEmpty()) return;

        log.info("Memulai pengiriman {} email undangan di background thread: {}", emailPeserta.size(), Thread.currentThread().getName());

        for (String email : emailPeserta) {
            if (email != null && !email.isBlank()) {
                try {
                    kirimSatuUndangan(dokumen, pengirim, email.trim());
                    log.info("Undangan berhasil dikirim ke {} untuk dokumen '{}'", email, dokumen.getNamaDokumen());
                } catch (Exception e) {
                    log.error("Gagal kirim undangan ke {}: {}", email, e.getMessage());
                }
            }
        }
    }

    private void kirimSatuUndangan(Dokumen dokumen, User pengirim, String toEmail)
            throws MessagingException, UnsupportedEncodingException {

        String subject = "📄 Anda diundang ke dokumen bersama: " + dokumen.getNamaDokumen();
        String dokumenUrl = baseUrl + "/dokumen/" + dokumen.getDokumenId();
        String html = buildInviteHtml(dokumen, pengirim, toEmail, dokumenUrl);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, fromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String buildInviteHtml(Dokumen dokumen, User pengirim, String toEmail, String dokumenUrl) {
        String kategori = dokumen.getKategori() != null ? dokumen.getKategori().getNamaKategori() : "Tidak dikategorikan";
        String tglMulai = dokumen.getTanggalMulai() != null ? dokumen.getTanggalMulai().toString() : "-";
        String tglBerakhir = dokumen.getTanggalBerakhir() != null ? dokumen.getTanggalBerakhir().toString() : "-";

        return "<!DOCTYPE html><html lang='id'><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f0f4f8;font-family:\"Segoe UI\",Arial,sans-serif;'>"
            + "<div style='max-width:580px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1);'>"

            // Header
            + "<div style='background:linear-gradient(135deg,#0f4c81,#1a6db5);padding:32px 36px;text-align:center;'>"
            + "<div style='width:56px;height:56px;background:linear-gradient(135deg,#f59e0b,#ff6b35);border-radius:14px;display:inline-flex;align-items:center;justify-content:center;font-size:26px;margin-bottom:14px;'>📄</div>"
            + "<h1 style='color:#fff;font-size:22px;font-weight:800;margin:0;'>Undangan Dokumen Bersama</h1>"
            + "<p style='color:rgba(255,255,255,.7);font-size:13px;margin:6px 0 0;'>DocMonitor • Sistem Monitoring Dokumen</p>"
            + "</div>"

            // Body
            + "<div style='padding:32px 36px;'>"
            + "<p style='font-size:15px;color:#374151;margin-bottom:20px;'>Halo,</p>"
            + "<p style='font-size:14px;color:#374151;line-height:1.6;margin-bottom:20px;'>"
            + "<strong style='color:#0f4c81;'>" + escHtml(pengirim.getName()) + "</strong>"
            + " mengundang Anda sebagai <strong>peserta</strong> dalam dokumen bersama berikut:</p>"

            // Dokumen card
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:20px;margin-bottom:24px;'>"
            + "<div style='display:flex;align-items:center;gap:12px;margin-bottom:14px;'>"
            + "<div style='width:42px;height:42px;background:#dbeafe;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0;'>📋</div>"
            + "<div><div style='font-size:16px;font-weight:700;color:#0f172a;'>" + escHtml(dokumen.getNamaDokumen()) + "</div>"
            + "<div style='font-size:12px;color:#64748b;'>Dokumen Bersama</div></div></div>"
            + "<table style='width:100%;border-collapse:collapse;'>"
            + tableRow("Kategori", kategori, "#f1f5f9")
            + tableRow("Tanggal Mulai", tglMulai, "#fff")
            + tableRow("Tanggal Berakhir", tglBerakhir, "#f1f5f9")
            + "</table></div>"

            // CTA Button
            + "<div style='text-align:center;margin-bottom:24px;'>"
            + "<a href='" + dokumenUrl + "' style='display:inline-block;padding:14px 32px;"
            + "background:linear-gradient(135deg,#0f4c81,#1a6db5);color:#fff;text-decoration:none;"
            + "border-radius:10px;font-size:15px;font-weight:700;"
            + "box-shadow:0 4px 12px rgba(15,76,129,.3);'>Lihat Dokumen →</a>"
            + "</div>"

            // Note
            + "<div style='background:#fef3c7;border-left:4px solid #f59e0b;padding:14px 16px;border-radius:0 8px 8px 0;margin-bottom:24px;'>"
            + "<p style='font-size:13px;color:#92400e;margin:0;'>⚠️ Jika Anda belum memiliki akun DocMonitor, "
            + "silakan <a href='" + baseUrl + "/auth/register' style='color:#0f4c81;font-weight:600;'>daftar terlebih dahulu</a> "
            + "untuk mengakses dokumen ini.</p></div>"

            + "<p style='font-size:12px;color:#94a3b8;line-height:1.6;'>"
            + "Email ini dikirim karena alamat <strong>" + escHtml(toEmail) + "</strong> "
            + "ditambahkan sebagai peserta oleh " + escHtml(pengirim.getName()) + ". "
            + "Jika ini adalah kesalahan, Anda dapat mengabaikan email ini.</p>"
            + "</div>"

            // Footer
            + "<div style='background:#f8fafc;padding:16px 36px;text-align:center;border-top:1px solid #e2e8f0;'>"
            + "<p style='font-size:12px;color:#94a3b8;margin:0;'>© 2026 DocMonitor • Sistem Monitoring Dokumen</p>"
            + "</div>"
            + "</div></body></html>";
    }

    private String tableRow(String label, String value, String bg) {
        return "<tr style='background:" + bg + ";'>"
            + "<td style='padding:9px 12px;border:1px solid #e2e8f0;font-size:13px;font-weight:600;color:#374151;width:40%;'>" + label + "</td>"
            + "<td style='padding:9px 12px;border:1px solid #e2e8f0;font-size:13px;color:#0f172a;'>" + escHtml(value) + "</td>"
            + "</tr>";
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * Kirim email notifikasi pengingat dokumen ke peserta (dokumen bersama).
     * Ditambahkan @Async agar scheduler yang memanggil tidak terhambat proses I/O.
     */
    @Async
    public void kirimNotifikasiKePeserta(Dokumen dokumen, String emailPeserta) {
        try {
            String subject;
            long sisaHari = dokumen.getSisaHari();
            if (sisaHari < 0) {
                subject = "⚠️ [KADALUARSA] Dokumen bersama: " + dokumen.getNamaDokumen();
            } else if (sisaHari == 0) {
                subject = "🔴 [HARI INI BERAKHIR] Dokumen bersama: " + dokumen.getNamaDokumen();
            } else {
                subject = "📋 [PENGINGAT] Dokumen bersama berakhir dalam " + sisaHari + " hari: " + dokumen.getNamaDokumen();
            }

            String html = buildReminderHtml(dokumen, emailPeserta);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailPeserta);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Notifikasi peserta dikirim ke {} untuk dokumen '{}'", emailPeserta, dokumen.getNamaDokumen());
        } catch (Exception e) {
            log.error("Gagal kirim notifikasi peserta ke {}: {}", emailPeserta, e.getMessage());
        }
    }

    private String buildReminderHtml(Dokumen dokumen, String emailPeserta) {
        String kategori = dokumen.getKategori() != null ? dokumen.getKategori().getNamaKategori() : "-";
        String statusText = dokumen.getSisaHari() < 0
            ? "telah <span style='color:#ef4444;font-weight:700;'>KADALUARSA</span>"
            : "akan berakhir dalam <strong>" + dokumen.getSisaHari() + " hari</strong>";

        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:580px;margin:auto;background:#f0f4f8;'>"
            + "<div style='max-width:580px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1);'>"
            + "<div style='background:linear-gradient(135deg,#0f4c81,#1a6db5);padding:24px 32px;'>"
            + "<h2 style='color:#fff;margin:0;font-size:18px;'>📄 Pengingat Dokumen Bersama</h2></div>"
            + "<div style='padding:28px 32px;'>"
            + "<p>Halo,</p>"
            + "<p>Dokumen bersama yang Anda ikuti " + statusText + ":</p>"
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:16px;margin:16px 0;'>"
            + "<strong style='font-size:15px;'>" + escHtml(dokumen.getNamaDokumen()) + "</strong>"
            + "<table style='width:100%;border-collapse:collapse;margin-top:12px;'>"
            + tableRow("Kategori", kategori, "#fff")
            + tableRow("Tanggal Berakhir", dokumen.getTanggalBerakhir() != null ? dokumen.getTanggalBerakhir().toString() : "-", "#f1f5f9")
            + tableRow("Status", dokumen.getStatus() != null ? dokumen.getStatus().getLabel() : "-", "#fff")
            + "</table></div>"
            + "<div style='text-align:center;margin:20px 0;'>"
            + "<a href='" + baseUrl + "/dokumen/" + dokumen.getDokumenId() + "' style='display:inline-block;padding:12px 28px;"
            + "background:#0f4c81;color:#fff;text-decoration:none;border-radius:8px;font-weight:700;'>Lihat Dokumen</a></div>"
            + "<p style='font-size:12px;color:#94a3b8;'>Email ini dikirim otomatis oleh DocMonitor. Jangan balas email ini.</p>"
            + "</div></div></body></html>";
    }
}