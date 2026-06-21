package com.bnpaper.agento.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepo;

    /**
     * Log an audit event asynchronously so it never blocks the request thread.
     * Uses REQUIRES_NEW so the audit write is independent of the caller's transaction.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, UUID entityId,
                    String details, String ipAddress) {
        String username = resolveUsername();
        try {
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .username(username)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();
            auditRepo.save(entry);
            log.debug("Audit: {} {} {} by {}", action, entityType, entityId, username);
        } catch (Exception e) {
            // Audit failures must never break business logic
            log.error("Failed to write audit log for action {}: {}", action, e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String details, String ipAddress) {
        log(action, null, null, details, ipAddress);
    }

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }
}
