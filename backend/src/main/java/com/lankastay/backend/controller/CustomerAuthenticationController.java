package com.lankastay.backend.controller;

import com.lankastay.backend.dto.auth.*;
import com.lankastay.backend.entity.CustomerUser;
import com.lankastay.backend.entity.SecurityEventType;
import com.lankastay.backend.service.CustomerAuthenticationService;
import com.lankastay.backend.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/auth")
public class CustomerAuthenticationController {

    private final CustomerAuthenticationService customerAuthService;
    private final SecurityAuditService audit;

    public CustomerAuthenticationController(CustomerAuthenticationService customerAuthService, SecurityAuditService audit) {
        this.customerAuthService = customerAuthService;
        this.audit = audit;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(
            @Valid @RequestBody CustomerRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        CustomerResponse response = customerAuthService.register(request, clientIp(servletRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerResponse> login(
            @Valid @RequestBody CustomerLoginRequest request,
            HttpServletRequest servletRequest
    ) {
        CustomerUser customer = customerAuthService.login(request, clientIp(servletRequest));

        HttpSession session = servletRequest.getSession(true);
        try {
            servletRequest.changeSessionId();
        } catch (IllegalStateException ignored) {
            // Already new or container does not support changeSessionId
        }
        session.setAttribute(com.lankastay.backend.service.CustomerSessionService.CUSTOMER_SESSION_KEY, customer.getId().toString());

        return ResponseEntity.ok(CustomerResponse.from(customer));
    }

    @PostMapping("/forgot-password/check-email")
    public ResponseEntity<Map<String, Boolean>> checkForgotPasswordEmail(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(Map.of("exists", customerAuthService.forgotPasswordEmailExists(request.email())));
    }

    @PostMapping("/forgot-password/change-password")
    public ResponseEntity<MessageResponse> changeForgottenPassword(
            @Valid @RequestBody SimpleForgotPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        customerAuthService.resetForgottenPassword(request, clientIp(servletRequest));
        return ResponseEntity.ok(new MessageResponse("Password changed successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> me(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session == null || session.getAttribute(com.lankastay.backend.service.CustomerSessionService.CUSTOMER_SESSION_KEY) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String idStr = (String) session.getAttribute(com.lankastay.backend.service.CustomerSessionService.CUSTOMER_SESSION_KEY);
        CustomerUser customer = customerAuthService.requireActive(java.util.UUID.fromString(idStr));
        return ResponseEntity.ok(CustomerResponse.from(customer));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            String idStr = (String) session.getAttribute(com.lankastay.backend.service.CustomerSessionService.CUSTOMER_SESSION_KEY);
            if (idStr != null) {
                audit.record(java.util.UUID.fromString(idStr), java.util.UUID.fromString(idStr), SecurityEventType.LOGOUT, clientIp(servletRequest), "SUCCESS");
            }
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
