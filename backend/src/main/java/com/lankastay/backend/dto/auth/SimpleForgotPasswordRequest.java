package com.lankastay.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.lankastay.backend.service.PasswordPolicy.MAX_LENGTH;
import static com.lankastay.backend.service.PasswordPolicy.MIN_LENGTH;

public record SimpleForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "New password is required")
        @Size(min = MIN_LENGTH, max = MAX_LENGTH, message = "Password must be between 8 and 128 characters")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        @Size(max = MAX_LENGTH, message = "Confirm password must be 128 characters or fewer")
        String confirmPassword
) {
    @Override public String toString() {
        return "SimpleForgotPasswordRequest[email=" + email + ", passwords=<redacted>]";
    }
}
