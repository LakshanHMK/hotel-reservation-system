package com.lankastay.backend.dto.staff;

import java.util.UUID;

public record ResetStaffPasswordResponse(UUID staffId, String temporaryPassword, boolean mustChangePassword) {
    @Override public String toString() {
        return "ResetStaffPasswordResponse[staffId=" + staffId
                + ", temporaryPassword=<redacted>, mustChangePassword=" + mustChangePassword + "]";
    }
}
