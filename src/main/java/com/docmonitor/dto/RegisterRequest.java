package com.docmonitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;
    
    @NotBlank(message = "Nama tidak boleh kosong")
    private String nama;
    
    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String password;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RegisterRequest() {}

    public RegisterRequest(String email, String nama, String password) {
        this.email = email;
        this.nama = nama;
        this.password = password;
    }
}