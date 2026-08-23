package com.lankastay.backend.repository;

import com.lankastay.backend.entity.StaffRole;
import com.lankastay.backend.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {
    Optional<StaffUser> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(StaffRole role);
}
