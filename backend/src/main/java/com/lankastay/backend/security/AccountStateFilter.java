package com.lankastay.backend.security;

import com.lankastay.backend.entity.StaffStatus;
import com.lankastay.backend.entity.StaffUser;
import com.lankastay.backend.repository.StaffUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccountStateFilter extends OncePerRequestFilter {
    private final StaffUserRepository users;
    public AccountStateFilter(StaffUserRepository users) { this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof StaffPrincipal principal) {
            StaffUser current = users.findById(principal.id()).orElse(null);
            if (current == null || current.getStatus() != StaffStatus.ACTIVE) {
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                SecurityContextHolder.clearContext();
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required.");
                return;
            }
            String path = request.getRequestURI();
            boolean initialPasswordEndpoint = path.equals("/api/v1/auth/change-initial-password")
                    || path.equals("/api/v1/auth/me") || path.equals("/api/v1/auth/logout") || path.equals("/api/v1/auth/csrf");
            if (current.isMustChangePassword() && !initialPasswordEndpoint) {
                // Security: a browser route cannot bypass the backend's forced initial-password gate.
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Initial password change is required.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"Security Error\",\"message\":\"" + message + "\"}");
    }
}
