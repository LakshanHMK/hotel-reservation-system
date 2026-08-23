package com.lankastay.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_audit", indexes = {
        @Index(name = "idx_security_audit_occurred", columnList = "occurred_at"),
        @Index(name = "idx_security_audit_target", columnList = "target_user_id")
})
public class SecurityAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "actor_user_id") private UUID actorUserId;
    @Column(name = "target_user_id") private UUID targetUserId;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private SecurityEventType eventType;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(nullable = false, length = 20) private String result;

    @PrePersist void onCreate() { if (occurredAt == null) occurredAt = Instant.now(); }
    public Long getId() { return id; }
    public UUID getActorUserId() { return actorUserId; }
    public void setActorUserId(UUID value) { actorUserId = value; }
    public UUID getTargetUserId() { return targetUserId; }
    public void setTargetUserId(UUID value) { targetUserId = value; }
    public SecurityEventType getEventType() { return eventType; }
    public void setEventType(SecurityEventType value) { eventType = value; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String value) { ipAddress = value; }
    public String getResult() { return result; }
    public void setResult(String value) { result = value; }
}
