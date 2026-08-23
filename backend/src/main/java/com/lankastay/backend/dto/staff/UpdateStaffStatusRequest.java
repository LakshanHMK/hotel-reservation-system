package com.lankastay.backend.dto.staff;

import com.lankastay.backend.entity.StaffStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStaffStatusRequest(@NotNull StaffStatus status) {}
