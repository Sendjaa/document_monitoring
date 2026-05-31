package com.docmonitor.service;

import com.docmonitor.model.User;
import com.docmonitor.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        // Cari atau buat user baru
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword("");
            newUser.setProvider("google");
            newUser.setProviderId(googleId);
            newUser.setRole("USER");
            newUser.setActive(true);
            newUser.setPhotoUrl(oAuth2User.getAttribute("picture"));
            return userRepository.save(newUser);
        });

        // Update nama jika berubah
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            userRepository.save(user);
        }

        String picture = oAuth2User.getAttribute("picture");
        if (picture != null && !picture.equals(user.getPhotoUrl())) {
            user.setPhotoUrl(picture);
            userRepository.save(user);
        }

        // Return wrapper yang implement OAuth2User sekaligus User entity
        return new OAuth2UserAdapter(user, oAuth2User.getAttributes());
    }

    // ── Inner class adapter ──────────────────────────────────────────
    public static class OAuth2UserAdapter extends User implements OAuth2User {

        private final Map<String, Object> attributes;
        private final String springSecurityName;

        public OAuth2UserAdapter(User user, Map<String, Object> attributes) {
            this.setUserId(user.getUserId());
            this.setName(user.getName());
            this.setEmail(user.getEmail());
            this.setPassword(user.getPassword() != null ? user.getPassword() : "");
            this.setRole(user.getRole());
            this.setActive(user.isActive());
            this.setProvider(user.getProvider());
            this.setProviderId(user.getProviderId());
            this.attributes = attributes;
            this.springSecurityName = user.getEmail();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + getRole()));
        }

        @Override
        public String getUsername() {
            return getEmail();
        }
    }
}