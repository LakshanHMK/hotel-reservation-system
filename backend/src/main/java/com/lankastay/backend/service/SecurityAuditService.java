package com.lankastay.backend.service;

import com.lankastay.backend.entity.SecurityAudit;
import com.lankastay.backend.entity.SecurityEventType;
import com.lankastay.backend.repository.SecurityAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SecurityAuditService {
    private final SecurityAuditRepository repository;
    public SecurityAuditService(SecurityAuditRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID actor, UUID target, SecurityEventType type, String ip, String result) {
        SecurityAudit event = new SecurityAudit();
        event.setActorUserId(actor);
        event.setTargetUserId(target);
        event.setEventType(type);
        event.setIpAddress(ip == null ? null : ip.substring(0, Math.min(ip.length(), 64)));
        event.setResult(result);
        // Security: audit metadata deliberately excludes passwords, temporary credentials, session IDs and tokens.
        repository.save(event);
    }
}
