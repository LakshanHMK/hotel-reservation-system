package com.lankastay.backend.dto.auth;

import com.lankastay.backend.entity.StaffRole;
import com.lankastay.backend.entity.StaffStatus;
import com.lankastay.backend.entity.StaffUser;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id, String email, String firstName, String lastName, StaffRole role,
        StaffStatus status, Long assignedHotelId, boolean mustChangePassword
) {
    public static CurrentUserResponse from(StaffUser user) {
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole(), user.getStatus(), user.getAssignedHotelId(), user.isMustChangePassword());
    }
}
