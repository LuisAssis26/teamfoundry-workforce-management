package com.teamfoundry.backend.account.service;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyActivitySectorsRepository;
import com.teamfoundry.backend.common.util.AccountCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Remove contas pendentes que ficaram abandonadas além do tempo limite configurado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingAccountCleanupJob {

    private final AccountRepository accountRepository;
    private final AccountCleanupService accountCleanupService;
    private final CompanyAccountOwnerRepository companyAccountOwnerRepository;
    private final CompanyActivitySectorsRepository companyActivitySectorsRepository;
    private final CompanyAccountRepository companyAccountRepository;

    @Value("${app.registration.pending-retention-minutes}")
    private long pendingRetentionMinutes;

    @Scheduled(fixedDelayString = "${app.registration.pending-cleanup-interval-ms}")
    @Transactional
    public void purgeStalePendingAccounts() {
        Instant threshold = Instant.now().minus(pendingRetentionMinutes, ChronoUnit.MINUTES);
        List<Account> staleAccounts = accountRepository.findByRegistrationStatusAndCreatedAtBefore(
                RegistrationStatus.PENDING,
                threshold
        );

        if (staleAccounts.isEmpty()) {
            return;
        }

        log.info("Removing {} pending accounts older than {} minutes", staleAccounts.size(), pendingRetentionMinutes);
        staleAccounts.forEach(this::removeAccountSafely);
    }

    private void removeAccountSafely(Account account) {
        try {
            if (account instanceof EmployeeAccount employeeAccount) {
                accountCleanupService.deleteEmployeeAccountByEmail(employeeAccount.getEmail());
                log.debug("Removed stale employee account {}", employeeAccount.getEmail());
            } else if (account instanceof CompanyAccount companyAccount) {
                cleanupCompanyAccount(companyAccount);
                log.debug("Removed stale company account {}", companyAccount.getEmail());
            } else {
                accountRepository.delete(account);
                log.debug("Removed stale account {}", account.getId());
            }
        } catch (Exception ex) {
            log.error("Failed to remove stale pending account id {} email {}", account.getId(), account.getEmail(), ex);
        }
    }

    private void cleanupCompanyAccount(CompanyAccount companyAccount) {
        companyActivitySectorsRepository.deleteByCompany(companyAccount);
        companyAccountOwnerRepository.deleteByCompanyAccount(companyAccount);
        companyAccountRepository.delete(companyAccount);
    }
}
