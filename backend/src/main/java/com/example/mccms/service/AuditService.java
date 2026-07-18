package com.example.mccms.service;

import com.example.mccms.model.AuditLog;
import com.example.mccms.model.User;
import com.example.mccms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User user, String action, String entity, String outcome) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .entity(entity)
                .outcome(outcome)
                .build();
        auditLogRepository.save(log);
    }
}
