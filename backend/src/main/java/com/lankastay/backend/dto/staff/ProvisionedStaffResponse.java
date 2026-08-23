package com.lankastay.backend.dto.staff;

public record ProvisionedStaffResponse(StaffResponse staff, String temporaryPassword) {
    @Override public String toString() { return "ProvisionedStaffResponse[staff=" + staff + ", temporaryPassword=<redacted>]"; }
}
