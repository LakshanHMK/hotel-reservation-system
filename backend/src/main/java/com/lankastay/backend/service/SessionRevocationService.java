package com.lankastay.backend.service;

import com.lankastay.backend.security.StaffPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

@Service
public class SessionRevocationService {
    private final SessionRegistry sessionRegistry;
    public SessionRevocationService(SessionRegistry sessionRegistry) { this.sessionRegistry = sessionRegistry; }

    public void revokeAll(String email) { revokeOther(email, null); }

    public void revokeOther(String email, String sessionToKeep) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof StaffPrincipal staff) || !staff.username().equalsIgnoreCase(email)) continue;
            for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                if (sessionToKeep == null || !session.getSessionId().equals(sessionToKeep)) session.expireNow();
            }
        }
    }
}
