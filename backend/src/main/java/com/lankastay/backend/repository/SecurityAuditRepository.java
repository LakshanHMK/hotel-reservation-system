package com.lankastay.backend.repository;

import com.lankastay.backend.entity.SecurityAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditRepository extends JpaRepository<SecurityAudit, Long> {}
