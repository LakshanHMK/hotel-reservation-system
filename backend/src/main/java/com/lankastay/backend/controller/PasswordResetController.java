package com.lankastay.backend.controller;

import com.lankastay.backend.dto.auth.ForgotPasswordRequest;
import com.lankastay.backend.dto.auth.MessageResponse;
import com.lankastay.backend.dto.auth.ResetPasswordRequest;
import com.lankastay.backend.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final Environment environment;

    public PasswordResetController(PasswordResetService passwordResetService, Environment environment) {
        this.passwordResetService = passwordResetService;
        this.environment = environment;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        String message = passwordResetService.requestPasswordReset(request, clientIp(servletRequest));
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        passwordResetService.resetPassword(request, clientIp(servletRequest));
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully. You may now log in with your new password."));
    }

    @GetMapping("/dev-last-reset-link")
    public ResponseEntity<Map<String, String>> getDevLastResetLink(@RequestParam(required = false) String email, HttpServletRequest request) {
        if (Arrays.stream(environment.getActiveProfiles()).noneMatch("dev"::equals)) {
            return ResponseEntity.notFound().build();
        }
        String ip = clientIp(request);
        if (!"127.0.0.1".equals(ip) && !"0:0:0:0:0:0:0:1".equals(ip) && !"localhost".equalsIgnoreCase(ip)) {
            return ResponseEntity.status(403).build();
        }
        String link = PasswordResetService.getDevLastResetLink(email);
        if (link == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("resetLink", link));
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
