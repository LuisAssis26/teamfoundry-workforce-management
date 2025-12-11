package com.teamfoundry.backend.superadmin.config.logs;

import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.auth.model.logs.AdminLogs;
import com.teamfoundry.backend.auth.model.logs.CommonLogs;
import com.teamfoundry.backend.auth.repository.logs.AdminLogsRepository;
import com.teamfoundry.backend.auth.repository.logs.CommonLogsRepository;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Popula a base com logs de admins e utilizadores para testes locais.
 */
@Configuration
@Profile("!test")
public class LogsSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogsSeeder.class);

    @Bean
    @Order(30)
    CommandLineRunner seedLogs(AdminLogsRepository adminLogsRepository,
                               CommonLogsRepository commonLogsRepository,
                               AdminAccountRepository adminAccountRepository,
                               AccountRepository accountRepository) {
        return args -> {
            if (adminLogsRepository.count() > 0 || commonLogsRepository.count() > 0) {
                LOGGER.debug("Log tables already populated; skipping log seeding.");
                return;
            }

            seedAdminLogs(adminLogsRepository, adminAccountRepository);
            seedUserLogs(commonLogsRepository, accountRepository);
        };
    }

    private void seedAdminLogs(AdminLogsRepository adminLogsRepository,
                               AdminAccountRepository adminAccountRepository) {
        List<AdminAccount> admins = adminAccountRepository.findAll();
        if (admins.isEmpty()) {
            LOGGER.warn("No admin accounts found; skipping admin log seeding.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<AdminLogs> logs = new ArrayList<>();

        admins.stream().limit(4).forEach(admin -> {
            logs.add(log(admin, "Criou conta de administrador secundário", now.minusDays(5)));
            logs.add(log(admin, "Atribuiu responsável a requisição crítica", now.minusDays(4)));
            logs.add(log(admin, "Aprovou credencial de empresa", now.minusDays(3)));
            logs.add(log(admin, "Rejeitou credencial de empresa por dados inválidos", now.minusDays(2)));
            logs.add(log(admin, "Editou privilégios de outro admin", now.minusDays(1)));
        });

        adminLogsRepository.saveAll(logs);
        LOGGER.info("Seeded {} admin log entries for testing.", logs.size());
    }

    private void seedUserLogs(CommonLogsRepository commonLogsRepository,
                              AccountRepository accountRepository) {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            LOGGER.warn("No accounts found; skipping user log seeding.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<CommonLogs> logs = new ArrayList<>();

        accounts.stream().limit(6).forEach(acc -> {
            logs.add(userLog(acc, "Atualizou fotografia de perfil", now.minusDays(7)));
            logs.add(userLog(acc, "Carregou currículo para a plataforma", now.minusDays(6)));
            logs.add(userLog(acc, "Aceitou uma requisição de trabalho", now.minusDays(5)));
            logs.add(userLog(acc, "Criou nova requisição de equipa", now.minusDays(4)));
            logs.add(userLog(acc, "Desativou a conta temporariamente", now.minusDays(3)));
        });

        commonLogsRepository.saveAll(logs);
        LOGGER.info("Seeded {} common log entries for testing.", logs.size());
    }

    private AdminLogs log(AdminAccount admin, String action, LocalDateTime timestamp) {
        AdminLogs entry = new AdminLogs();
        entry.setAdmin(admin);
        entry.setAction(action);
        entry.setTimestamp(timestamp);
        return entry;
    }

    private CommonLogs userLog(Account account, String action, LocalDateTime timestamp) {
        CommonLogs entry = new CommonLogs();
        entry.setUser(account);
        entry.setAction(action);
        entry.setTimestamp(timestamp);
        return entry;
    }
}
