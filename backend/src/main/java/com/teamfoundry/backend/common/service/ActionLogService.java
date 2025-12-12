package com.teamfoundry.backend.common.service;

import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.auth.model.logs.AdminLogs;
import com.teamfoundry.backend.auth.model.logs.CommonLogs;
import com.teamfoundry.backend.auth.repository.logs.AdminLogsRepository;
import com.teamfoundry.backend.auth.repository.logs.CommonLogsRepository;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Serviço utilitário para registar ações relevantes (auditoria simples).
 */
@Service
@RequiredArgsConstructor
public class ActionLogService {

    private static final Logger log = LoggerFactory.getLogger(ActionLogService.class);

    private final AdminLogsRepository adminLogsRepository;
    private final CommonLogsRepository commonLogsRepository;

    public void logAdmin(AdminAccount admin, String action) {
        if (admin == null || action == null || action.isBlank()) return;
        try {
            AdminLogs entry = new AdminLogs();
            entry.setAdmin(admin);
            entry.setAction(action);
            entry.setTimestamp(LocalDateTime.now());
            adminLogsRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Não foi possível registar log de admin: {}", ex.getMessage());
        }
    }

    public void logUser(Account account, String action) {
        if (account == null || action == null || action.isBlank()) return;
        try {
            CommonLogs entry = new CommonLogs();
            entry.setUser(account);
            entry.setAction(action);
            entry.setTimestamp(LocalDateTime.now());
            commonLogsRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Não foi possível registar log de utilizador: {}", ex.getMessage());
        }
    }
}
