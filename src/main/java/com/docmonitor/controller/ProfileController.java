package com.docmonitor.controller;

import com.docmonitor.dto.UserRequestDTO;
import com.docmonitor.model.User;
import com.docmonitor.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profil")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfil(@AuthenticationPrincipal User currentUser, Model model) {
        User user = userService.findById(currentUser.getUserId());
        model.addAttribute("currentUser", user);
        model.addAttribute("activeMenu", "profil");
        return "profil/index";
    }

    @PostMapping("/update")
    public String updateProfil(@AuthenticationPrincipal User currentUser,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam(value = "password", required = false) String password,
            RedirectAttributes redirectAttributes) {
        try {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setName(name);
            dto.setPhone(phone);
            dto.setPassword(password);
            userService.updateProfile(currentUser.getUserId(), dto);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal update profil: " + e.getMessage());
        }
        return "redirect:/profil";
    }
}