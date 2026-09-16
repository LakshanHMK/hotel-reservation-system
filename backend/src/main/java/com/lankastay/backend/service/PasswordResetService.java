package com.lankastay.backend.service;

import com.lankastay.backend.dto.auth.ForgotPasswordRequest;
import com.lankastay.backend.dto.auth.ResetPasswordRequest;
import com.lankastay.backend.entity.CustomerUser;
import com.lankastay.backend.entity.PasswordResetToken;
import com.lankastay.backend.entity.SecurityEventType;
import com.lankastay.backend.entity.StaffUser;
import com.lankastay.backend.exception.ApiException;
import com.lankastay.backend.repository.CustomerUserRepository;
import com.lankastay.backend.repository.PasswordResetTokenRepository;
import com.lankastay.backend.repository.StaffUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String GENERIC_RESPONSE = "If an account exists for that email, password reset instructions have been sent.";

    private final StaffUserRepository staffRepository;
    private final CustomerUserRepository customerRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final PasswordPolicy passwordPolicy;
    private final SecurityAuditService audit;
    private final Environment environment;

    // Development helper store for local QA testing without external SMTP credentials
    private static final ConcurrentHashMap<String, String> devLastResetLinks = new ConcurrentHashMap<>();

    public PasswordResetService(StaffUserRepository staffRepository, CustomerUserRepository customerRepository,
                                PasswordResetTokenRepository tokenRepository, PasswordEncoder encoder,
                                PasswordPolicy passwordPolicy, SecurityAuditService audit, Environment environment) {
        this.staffRepository = staffRepository;
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.encoder = encoder;
        this.passwordPolicy = passwordPolicy;
        this.audit = audit;
        this.environment = environment;
    }

    @Transactional
    public String requestPasswordReset(ForgotPasswordRequest request, String ipAddress) {
        String email = CustomerUser.normalizeEmail(request.email());
        if (email == null || email.isBlank()) {
            return GENERIC_RESPONSE;
        }

        Optional<StaffUser> staffOpt = staffRepository.findByEmail(email);
        Optional<CustomerUser> customerOpt = customerRepository.findByEmail(email);

        if (staffOpt.isEmpty() && customerOpt.isEmpty()) {
            audit.record(null, null, SecurityEventType.PASSWORD_RESET_REQUESTED, ipAddress, "GENERIC_OK");
            return GENERIC_RESPONSE;
        }

        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(tokenHash);
        token.setExpiresAt(Instant.now().plusSeconds(1800)); // 30 minutes validity

        if (staffOpt.isPresent()) {
            StaffUser staff = staffOpt.get();
            tokenRepository.deleteByStaffUserId(staff.getId());
            token.setStaffUserId(staff.getId());
            audit.record(staff.getId(), staff.getId(), SecurityEventType.PASSWORD_RESET_REQUESTED, ipAddress, "SUCCESS");
            
            rememberDevelopmentLink(email, "http://localhost:5174/staff/reset-password?token=" + rawToken);
            logger.debug("Generated staff password reset token for account.");
        } else if (customerOpt.isPresent()) {
            CustomerUser customer = customerOpt.get();
            tokenRepository.deleteByCustomerUserId(customer.getId());
            token.setCustomerUserId(customer.getId());
            audit.record(customer.getId(), customer.getId(), SecurityEventType.PASSWORD_RESET_REQUESTED, ipAddress, "SUCCESS");

            rememberDevelopmentLink(email, "http://localhost:5174/reset-password?token=" + rawToken);
            logger.debug("Generated customer password reset token for account.");
        }

        tokenRepository.save(token);
        return GENERIC_RESPONSE;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String ipAddress) {
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Validation Error", "New password and confirmation do not match.");
        }

        passwordPolicy.validate(request.newPassword());

        String submittedHash = hashToken(request.token());
        PasswordResetToken token = tokenRepository.findByTokenHashAndUsedAtIsNull(submittedHash)
                .orElseThrow(() -> {
                    audit.record(null, null, SecurityEventType.PASSWORD_RESET_FAILED, ipAddress, "INVALID_TOKEN");
                    return new ApiException(HttpStatus.BAD_REQUEST, "Bad Request", "Password reset token is invalid, expired, or has already been used.");
                });

        if (token.isExpired()) {
            audit.record(null, null, SecurityEventType.PASSWORD_RESET_FAILED, ipAddress, "EXPIRED_TOKEN");
            throw new ApiException(HttpStatus.BAD_REQUEST, "Bad Request", "Password reset token has expired. Please request a new link.");
        }

        String newHash = encoder.encode(request.newPassword());

        if (token.getStaffUserId() != null) {
            StaffUser staff = staffRepository.findById(token.getStaffUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Not Found", "Associated staff user no longer exists."));

            if (encoder.matches(request.newPassword(), staff.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Validation Error", "New password must be different from current password.");
            }

            staff.setPasswordHash(newHash);
            staff.setMustChangePassword(false);
            staff.setFailedLoginAttempts(0);
            staff.setLockedUntil(null);
            staffRepository.save(staff);
            audit.record(staff.getId(), staff.getId(), SecurityEventType.PASSWORD_RESET_COMPLETED, ipAddress, "SUCCESS");
        } else if (token.getCustomerUserId() != null) {
            CustomerUser customer = customerRepository.findById(token.getCustomerUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Not Found", "Associated customer user no longer exists."));

            if (encoder.matches(request.newPassword(), customer.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Validation Error", "New password must be different from current password.");
            }

            customer.setPasswordHash(newHash);
            customer.setFailedLoginAttempts(0);
            customer.setLockedUntil(null);
            customerRepository.save(customer);
            audit.record(customer.getId(), customer.getId(), SecurityEventType.PASSWORD_RESET_COMPLETED, ipAddress, "SUCCESS");
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Bad Request", "Orphaned reset token.");
        }

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    public static String getDevLastResetLink(String email) {
        if (email != null && !email.isBlank()) {
            return devLastResetLinks.get(CustomerUser.normalizeEmail(email));
        }
        return devLastResetLinks.get("last");
    }

    private void rememberDevelopmentLink(String email, String resetUrl) {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equals(profile)) {
                devLastResetLinks.put("last", resetUrl);
                devLastResetLinks.put(email, resetUrl);
                return;
            }
        }
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
