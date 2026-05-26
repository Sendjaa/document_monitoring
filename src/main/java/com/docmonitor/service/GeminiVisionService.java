package com.docmonitor.service;

import com.docmonitor.dto.DokumenExtractDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiVisionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Ekstrak data dokumen dari gambar menggunakan Gemini Vision API.
     * 
     * @param imageFile file gambar/foto dokumen
     * @return DTO berisi data yang diekstrak
     */
    public DokumenExtractDTO ekstrakDariGambar(MultipartFile imageFile) throws IOException {
        log.info("Memulai ekstraksi dokumen dari gambar: {}", imageFile.getOriginalFilename());

        // Convert gambar ke Base64
        byte[] imageBytes = imageFile.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg";

        // Buat prompt untuk Gemini
        String prompt = buildPrompt();

        // Build request body JSON
        String requestBody = buildRequestBody(base64Image, mimeType, prompt);

        // Kirim request ke Gemini API
        String responseJson = callGeminiApi(requestBody);

        // Parse response
        return parseGeminiResponse(responseJson);
    }

    private String buildPrompt() {
        return """
                Tugas Anda adalah menjadi ekstraktor data dokumen yang sangat presisi.
                Analisis gambar ini dan ekstrak informasi ke format JSON.

                ATURAN KETAT:
                1. HANYA ekstrak informasi yang TERLIHAT JELAS dalam dokumen.
                2. Jika informasi (seperti tanggal atau nama) TIDAK DITEMUKAN, isi dengan null. JANGAN MENGARANG atau MENEBAK.
                3. Pastikan format tanggal adalah YYYY-MM-DD.
                4. HANYA kembalikan JSON murni, tanpa pembuka/penutup markdown, tanpa basa-basi.

                Format JSON:
                {
                  "namaDokumen": "Tulis judul dokumen. Jika tidak ada judul, gunakan nama jenis dokumen yang terlihat.",
                  "tanggalMulai": "YYYY-MM-DD atau null",
                  "tanggalBerakhir": "YYYY-MM-DD atau null",
                  "namaKategori": "Pilih dari kategori: KTP, SIM, Paspor, STNK, Kontrak, Sertifikat, Ijazah, Lainnya.",
                  "deskripsiTambahan": "Informasi penting lainnya atau null"
                }
                """;
    }

    private String buildRequestBody(String base64Image, String mimeType, String prompt) throws IOException {
        String requestTemplate = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        },
                        {
                          "inline_data": {
                            "mime_type": "%s",
                            "data": "%s"
                          }
                        }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "temperature": 0.0,
                    "response_mime_type": "application/json"
                  }
                }
                """;

        // Escape prompt untuk JSON
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return String.format(requestTemplate, escapedPrompt, mimeType, base64Image);
    }

    private String callGeminiApi(String requestBody) throws IOException {
        String fullUrl = apiUrl + "?key=" + apiKey;

        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Gemini API error: {} - {}", response.code(), errorBody);
                throw new IOException("Gemini API gagal: " + response.code() + " - " + errorBody);
            }

            String responseStr = response.body() != null ? response.body().string() : "";
            log.debug("Gemini response: {}", responseStr);
            return responseStr;
        }
    }

    private DokumenExtractDTO parseGeminiResponse(String responseJson) {
        DokumenExtractDTO dto = new DokumenExtractDTO();

        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // Ambil teks dari response Gemini
            String textContent = root
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")   
                    .asText("");

            log.info("Gemini extracted text: {}", textContent);

            // Bersihkan markdown jika ada
            textContent = cleanJsonString(textContent);

            // Parse JSON hasil ekstraksi
            JsonNode extracted = objectMapper.readTree(textContent);

            dto.setNamaDokumen(getTextOrDefault(extracted, "namaDokumen", "Dokumen Tidak Diketahui"));
            dto.setNamaKategori(getTextOrDefault(extracted, "namaKategori", null));
            dto.setDeskripsiTambahan(getTextOrDefault(extracted, "deskripsiTambahan", null));
            dto.setTanggalMulai(parseDate(getTextOrDefault(extracted, "tanggalMulai", null)));
            dto.setTanggalBerakhir(parseDate(getTextOrDefault(extracted, "tanggalBerakhir", null)));

            log.info("Berhasil ekstrak dokumen: nama={}, kategori={}, berakhir={}",
                    dto.getNamaDokumen(), dto.getNamaKategori(), dto.getTanggalBerakhir());

        } catch (Exception e) {
            log.error("Gagal parse response Gemini: {}", e.getMessage());
            dto.setNamaDokumen("Dokumen (Ekstraksi Gagal)");
            dto.setDeskripsiTambahan("Gagal mengekstrak data: " + e.getMessage());
        }

        return dto;
    }

    private String cleanJsonString(String text) {
        if (text == null || text.isBlank())
            return "{}";
        // Remove markdown code blocks
        text = text.trim();
        if (text.startsWith("```json"))
            text = text.substring(7);
        if (text.startsWith("```"))
            text = text.substring(3);
        if (text.endsWith("```"))
            text = text.substring(0, text.length() - 3);
        return text.trim();
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isNull() || fieldNode.isMissingNode() || fieldNode.asText().equalsIgnoreCase("null")) {
            return defaultValue;
        }
        return fieldNode.asText().trim();
        
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equalsIgnoreCase("null")) {
            return null;
        }
        // Coba berbagai format tanggal
        String[] formats = { "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy/MM/dd" };
        for (String fmt : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {
            }
        }
        log.warn("Tidak dapat parse tanggal: {}", dateStr);
        return null;
    }
    
    @SuppressWarnings("unused")
    private void validateResult(JsonNode node) {
    // Pastikan nama dokumen tidak berisi teks default AI yang aneh
    String nama = node.path("namaDokumen").asText();
    if (nama.contains("mungkin") || nama.contains("sepertinya")) {
        throw new RuntimeException("AI memberikan jawaban spekulatif, membatalkan ekstraksi.");
    }
}
}
