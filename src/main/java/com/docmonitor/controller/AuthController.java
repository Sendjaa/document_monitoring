package com.docmonitor.controller;

import com.docmonitor.dto.UserRequestDTO;
import com.docmonitor.service.UserService;
import com.docmonitor.repository.DokumenPesertaRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.docmonitor.model.User;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final DokumenPesertaRepository dokumenPesertaRepository;

    // Constructor
    public AuthController(UserService userService, DokumenPesertaRepository dokumenPesertaRepository) {
        this.userService = userService;
        this.dokumenPesertaRepository = dokumenPesertaRepository;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("error", "Email atau password salah!");
        if (logout != null)
            model.addAttribute("logout", "Anda telah berhasil keluar.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDTO", new UserRequestDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerProcess(@Valid @ModelAttribute("userDTO") UserRequestDTO dto,
            @RequestParam(required = false) String inviteToken,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors())
            return "auth/register";

        try {
            // 1. Simpan user
            User newUser = userService.registerUser(dto);

            // 2. OTOMATIS: Hubungkan dokumen berdasarkan email (JIKA ADA UNDANGAN
            // SEBELUMNYA)
            // Ini akan memproses baris-baris data yang emailnya sudah ada di DokumenPeserta
            // Panggil method hubungkanDokumenSetelahRegister (Anda harus membuatnya public
            // atau pindahkan logic ini ke sini)
            userService.hubungkanDokumenSetelahRegister(newUser);

            // 3. JIKA ADA TOKEN: Hubungkan dokumen spesifik berdasarkan token
            if (inviteToken != null && !inviteToken.isEmpty()) {
                dokumenPesertaRepository.findByInviteToken(inviteToken)
                        .ifPresent(peserta -> {
                            peserta.setUser(newUser);
                            peserta.setAccepted(true);
                            dokumenPesertaRepository.save(peserta);
                        });
            }

            redirectAttributes.addFlashAttribute("success", "Registrasi berhasil!");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "redirect:/auth/logout";
    }
}
