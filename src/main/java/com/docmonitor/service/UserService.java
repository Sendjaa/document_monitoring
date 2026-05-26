package com.docmonitor.service;

import com.docmonitor.dto.UserRequestDTO;
import com.docmonitor.model.DokumenPeserta;
import com.docmonitor.model.User;
import com.docmonitor.repository.DokumenPesertaRepository;
import com.docmonitor.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.docmonitor.dto.RegisterRequest;

import java.util.List;

@Service
// @Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final DokumenPesertaRepository dokumenPesertaRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor Injection
    public UserService(UserRepository userRepository, DokumenPesertaRepository dokumenPesertaRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dokumenPesertaRepository = dokumenPesertaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getNama());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        hubungkanDokumenSetelahRegister(savedUser);
        return savedUser;
    }

    public void hubungkanDokumenSetelahRegister(User user) {
        List<DokumenPeserta> daftarUndangan = dokumenPesertaRepository
                .findByEmailPesertaAndAcceptedFalse(user.getEmail());
        for (DokumenPeserta peserta : daftarUndangan) {
            peserta.setUser(user);
            peserta.setAccepted(true);
            dokumenPesertaRepository.save(peserta);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + email));
    }

    public User registerUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email sudah terdaftar: " + dto.getEmail());
        }

        // Variabel user dibuat di dalam method (Local Variable) agar lebih aman
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role("USER")
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User baru terdaftar: {}", saved.getEmail());
        return saved;
    }

    public User updateProfile(Long userId, UserRequestDTO dto) {
        User user = findById(userId);
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan ID: " + userId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + email));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deactivateUser(Long userId) {
        User user = findById(userId);
        user.setActive(false);
        userRepository.save(user);
    }
}