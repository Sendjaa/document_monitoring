package com.docmonitor.service;

import com.docmonitor.model.Dokumen;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotifikasi extends BaseNotifikasi {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifikasi.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    private static final String TEMPLATE =
        "Dokumen: <strong>%s</strong><br>" +
        "Status: %s<br>" +
        "Tanggal Berakhir: %s<br><br>" +
        "%s";

    public EmailNotifikasi(JavaMailSender mailSender) {
        super(TEMPLATE, "EMAIL");
        this.mailSender = mailSender;
    }

    @Override
    public void kirimNotifikasi(Dokumen doc) {
        if (doc.getUser() == null || doc.getUser().getEmail() == null) {
            log.warn("Tidak dapat kirim email: user atau email kosong untuk dokumen ID {}", doc.getDokumenId());
            return;
        }
        try {
            String toEmail = doc.getUser().getEmail();
            String subject = buildSubject(doc);
            String body = buildHtmlBody(doc);

            sendHtmlEmail(toEmail, subject, body);
            logAktivitas();
            log.info("Email berhasil dikirim ke {} untuk dokumen: {}", toEmail, doc.getNamaDokumen());
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Gagal mengirim email untuk dokumen ID {}: {}", doc.getDokumenId(), e.getMessage());
        }
    }

    private String buildSubject(Dokumen doc) {
        long sisaHari = doc.getSisaHari();
        if (sisaHari < 0) {
            return "⚠️ [KADALUARSA] Dokumen: " + doc.getNamaDokumen();
        } else if (sisaHari == 0) {
            return "🔴 [HARI INI BERAKHIR] Dokumen: " + doc.getNamaDokumen();
        } else {
            return "📋 [PENGINGAT] Dokumen berakhir dalam " + sisaHari + " hari: " + doc.getNamaDokumen();
        }
    }

    private String buildHtmlBody(Dokumen doc) {
        String statusText = doc.getSisaHari() < 0
            ? "telah <span style='color:red;font-weight:bold'>KADALUARSA</span>"
            : "akan berakhir dalam <strong>" + doc.getSisaHari() + " hari</strong>";

        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>" +
            "<div style='background:#1e3a5f;padding:20px;border-radius:8px 8px 0 0;'>" +
            "<h2 style='color:white;margin:0;'>📄 Document Monitoring System</h2>" +
            "</div>" +
            "<div style='border:1px solid #e0e0e0;padding:30px;border-radius:0 0 8px 8px;'>" +
            "<p>Yth. <strong>" + doc.getUser().getName() + "</strong>,</p>" +
            "<p>Kami ingin menginformasikan bahwa dokumen berikut " + statusText + ":</p>" +
            "<table style='width:100%;border-collapse:collapse;margin:15px 0;'>" +
            "<tr style='background:#f5f5f5;'>" +
            "<td style='padding:10px;border:1px solid #ddd;font-weight:bold;width:40%;'>Nama Dokumen</td>" +
            "<td style='padding:10px;border:1px solid #ddd;'>" + doc.getNamaDokumen() + "</td></tr>" +
            "<tr><td style='padding:10px;border:1px solid #ddd;font-weight:bold;'>Kategori</td>" +
            "<td style='padding:10px;border:1px solid #ddd;'>" +
            (doc.getKategori() != null ? doc.getKategori().getNamaKategori() : "Tidak dikategorikan") + "</td></tr>" +
            "<tr style='background:#f5f5f5;'><td style='padding:10px;border:1px solid #ddd;font-weight:bold;'>Tanggal Mulai</td>" +
            "<td style='padding:10px;border:1px solid #ddd;'>" + (doc.getTanggalMulai() != null ? doc.getTanggalMulai() : "-") + "</td></tr>" +
            "<tr><td style='padding:10px;border:1px solid #ddd;font-weight:bold;'>Tanggal Berakhir</td>" +
            "<td style='padding:10px;border:1px solid #ddd;color:" + (doc.getSisaHari() < 0 ? "red" : "orange") + ";font-weight:bold;'>" +
            doc.getTanggalBerakhir() + "</td></tr>" +
            "<tr style='background:#f5f5f5;'><td style='padding:10px;border:1px solid #ddd;font-weight:bold;'>Status</td>" +
            "<td style='padding:10px;border:1px solid #ddd;'>" + doc.getStatus().getLabel() + "</td></tr>" +
            "</table>" +
            "<p>Harap segera lakukan tindakan yang diperlukan untuk memperbarui atau memperpanjang dokumen tersebut.</p>" +
            "<div style='background:#fff3cd;border-left:4px solid #ffc107;padding:12px;margin:15px 0;'>" +
            "<strong>⚠️ Segera akses sistem untuk melakukan pembaruan dokumen.</strong></div>" +
            "<p style='color:#666;font-size:12px;margin-top:30px;border-top:1px solid #eee;padding-top:15px;'>" +
            "Email ini dikirim otomatis oleh Document Monitoring System.<br>" +
            "Jangan balas email ini.</p>" +
            "</div></body></html>";
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
