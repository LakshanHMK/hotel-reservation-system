package com.lankastay.backend.dto.auth;

import com.lankastay.backend.entity.CustomerUser;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String role,
        String status,
        Instant createdAt
) {
    public static CustomerResponse from(CustomerUser customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone(),
                customer.getRole(),
                customer.getStatus(),
                customer.getCreatedAt()
        );
    }
}
