package com.docmonitor.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    private String phone;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private String provider = "local";

    @Column
    private String providerId;

    @Column(name = "photo_url")
    private String photoUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dokumen> dokumenList = new ArrayList<>();

    // Constructors
    public User() {}

    public User(String name, String email, String password, String phone, String role, boolean active) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    // Getters and Setters

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Dokumen> getDokumenList() { return dokumenList; }
    public void setDokumenList(List<Dokumen> dokumenList) { this.dokumenList = dokumenList; }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // Builder pattern implementation
    public static class UserBuilder {
        private String name;
        private String email;
        private String password;
        private String phone;
        private String role = "USER";
        private boolean active = true;

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public User build() {
            User user = new User();
            user.name = this.name;
            user.email = this.email;
            user.password = this.password;
            user.phone = this.phone;
            user.role = this.role;
            user.active = this.active;
            return user;
        }
    }

    // =============================================
    // UserDetails methods (Spring Security)
    // =============================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }

    // =============================================
    // Business methods
    // =============================================

    public void addDocument(Dokumen doc) {
        dokumenList.add(doc);
        doc.setUser(this);
    }

    public List<Dokumen> viewDocuments() {
        return dokumenList;
    }

}
