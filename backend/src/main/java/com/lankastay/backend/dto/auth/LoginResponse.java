package com.lankastay.backend.dto.auth;

public record LoginResponse(String status, CurrentUserResponse user) {}
