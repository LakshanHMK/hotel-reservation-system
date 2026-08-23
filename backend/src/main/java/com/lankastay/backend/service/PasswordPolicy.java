package com.lankastay.backend.service;

import com.lankastay.backend.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {
    public void validate(String password) {
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Validation Error", "Password must be between 12 and 128 characters.");
        }
        if (password.chars().allMatch(Character::isWhitespace)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Validation Error", "Password cannot contain only whitespace.");
        }
    }
}
