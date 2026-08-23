package com.lankastay.backend.dto.staff;

import com.lankastay.backend.entity.StaffRole;
import com.lankastay.backend.entity.StaffStatus;
import com.lankastay.backend.entity.StaffUser;

import java.time.Instant;
import java.util.UUID;

public record StaffResponse(
        UUID id, String firstName, String lastName, String email, StaffRole role,
        StaffStatus status, Long assignedHotelId, boolean mustChangePassword,
        Instant lastLoginAt, Instant createdAt, Instant updatedAt
) {
    public static StaffResponse from(StaffUser user) {
        return new StaffResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getRole(), user.getStatus(), user.getAssignedHotelId(), user.isMustChangePassword(),
                user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
