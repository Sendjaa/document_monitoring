package com.docmonitor.service;

import com.docmonitor.dto.DokumenExtractDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public DokumenExtractDTO ekstrakDariGambar(MultipartFile imageFile) throws IOException {
        log.info("Memulai ekstraksi dokumen dari gambar: {}", imageFile.getOriginalFilename());

        byte[] imageBytes = imageFile.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg";

        String requestBody = buildRequestBody(base64Image, mimeType, buildPrompt());
        String responseJson = callGeminiApi(requestBody);
        return parseGeminiResponse(responseJson);
    }

    private String buildPrompt() {
        return """
                Tugas Anda adalah menjadi ekstraktor data dokumen yang sangat presisi.
                Analisis gambar ini dan ekstrak informasi ke format JSON.
                ATURAN KETAT:
                1. HANYA ekstrak informasi yang TERLIHAT JELAS dalam dokumen.
                2. Jika informasi TIDAK DITEMUKAN, isi dengan null. JANGAN MENGARANG.
                3. Format tanggal adalah YYYY-MM-DD.
                4. HANYA kembalikan JSON murni, tanpa markdown, tanpa basa-basi.
                Format JSON:
                {
                  "namaDokumen": "judul atau jenis dokumen",
                  "tanggalMulai": "YYYY-MM-DD atau null",
                  "tanggalBerakhir": "YYYY-MM-DD atau null",
                  "namaKategori": "KTP, SIM, Paspor, STNK, Kontrak, Sertifikat, Ijazah, atau Lainnya",
                  "deskripsiTambahan": "informasi penting lainnya atau null"
                }
                """;
    }

    // Format request body Gemini (bukan OpenAI)
    private String buildRequestBody(String base64Image, String mimeType, String prompt) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();

        // contents array
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");

        // Text part
        parts.addObject().put("text", prompt);

        // Image part - format Gemini pakai inline_data
        ObjectNode imagePart = parts.addObject();
        ObjectNode inlineData = imagePart.putObject("inline_data");
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);

        // generation_config
        ObjectNode genConfig = root.putObject("generation_config");
        genConfig.put("temperature", 0.0);
        genConfig.put("response_mime_type", "application/json");

        return objectMapper.writeValueAsString(root);
    }

    private String callGeminiApi(String requestBody) throws IOException {
        String cleanUrl = apiUrl.trim();
        String cleanKey = apiKey.trim();
        String fullUrl = cleanUrl + "?key=" + cleanKey;

        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 429) {
                throw new IOException("Quota API habis. Silakan coba beberapa saat lagi.");
            }
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

    // Format parsing response Gemini (bukan OpenAI choices[])
    private DokumenExtractDTO parseGeminiResponse(String responseJson) {
        DokumenExtractDTO dto = new DokumenExtractDTO();
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // Format Gemini: candidates[0].content.parts[0].text
            String textContent = root
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("");

            log.info("Gemini extracted text: {}", textContent);
            textContent = cleanJsonString(textContent);

            JsonNode extracted = objectMapper.readTree(textContent);

            dto.setNamaDokumen(getTextOrDefault(extracted, "namaDokumen", "Dokumen Tidak Diketahui"));
            dto.setNamaKategori(getTextOrDefault(extracted, "namaKategori", "Lainnya"));
            dto.setDeskripsiTambahan(getTextOrDefault(extracted, "deskripsiTambahan", null));
            dto.setTanggalMulai(parseDate(getTextOrDefault(extracted, "tanggalMulai", null)));
            dto.setTanggalBerakhir(parseDate(getTextOrDefault(extracted, "tanggalBerakhir", null)));

            log.info("Berhasil ekstrak: nama={}, kategori={}, berakhir={}",
                    dto.getNamaDokumen(), dto.getNamaKategori(), dto.getTanggalBerakhir());

        } catch (Exception e) {
            log.error("Gagal parse response: {}", e.getMessage());
            dto.setNamaDokumen("Dokumen (Ekstraksi Gagal)");
            dto.setDeskripsiTambahan("Gagal mengekstrak data: " + e.getMessage());
        }
        return dto;
    }

    private String cleanJsonString(String text) {
        if (text == null || text.isBlank()) return "{}";
        text = text.trim();
        if (text.startsWith("```json")) text = text.substring(7);
        if (text.startsWith("```")) text = text.substring(3);
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
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
        if (dateStr == null || dateStr.isBlank() || dateStr.equalsIgnoreCase("null")) return null;
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy/MM/dd"};
        for (String fmt : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {}
        }
        log.warn("Tidak dapat parse tanggal: {}", dateStr);
        return null;
    }
}