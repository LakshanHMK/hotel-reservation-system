package com.lankastay.backend.dto.staff;

import com.lankastay.backend.entity.StaffRole;
import jakarta.validation.constraints.NotNull;

public record UpdateStaffRequest(@NotNull StaffRole role, Long assignedHotelId) {}
