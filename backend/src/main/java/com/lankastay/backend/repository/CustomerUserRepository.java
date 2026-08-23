package com.lankastay.backend.repository;

import com.lankastay.backend.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, UUID> {
    Optional<CustomerUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
