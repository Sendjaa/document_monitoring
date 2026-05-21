package com.docmonitor.repository;

import com.docmonitor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Cari user berdasarkan email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Cek apakah email sudah ada.
     */
    boolean existsByEmail(String email);

    /**
     * Cari user berdasarkan email (untuk login).
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForLogin(@Param("email") String email);
}
