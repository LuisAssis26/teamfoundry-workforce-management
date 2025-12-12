package com.teamfoundry.backend.superadmin.service.credentials;

import com.teamfoundry.backend.superadmin.dto.credential.company.CompanyCredentialResponse;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.enums.UserType;
import lombok.RequiredArgsConstructor;
import com.teamfoundry.backend.common.service.ActionLogService;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyActivitySectorsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyCredentialService {

    private static final String ADMIN_TOKEN_PREFIX = "admin:";

    private final CompanyAccountRepository companyAccountRepository;
    private final CompanyAccountOwnerRepository companyAccountOwnerRepository;
    private final CompanyActivitySectorsRepository companyActivitySectorsRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;

    public List<CompanyCredentialResponse> listPendingCompanyCredentials() {
        return companyAccountRepository.findPendingCompanyCredentials();
    }

    @Transactional
    public void approveCompanyCredential(int companyId, String superAdminPassword) {
        validateSuperAdminPassword(superAdminPassword);

        CompanyAccount company = companyAccountRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        if (company.isStatus()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Empresa já aprovada.");
        }

        company.setStatus(true);
        companyAccountRepository.save(company);
        AdminAccount requester = resolveAuthenticatedAdmin();
        actionLogService.logAdmin(requester, "Aprovou credencial da empresa " + company.getName());
    }

    @Transactional
    public void rejectCompanyCredential(int companyId, String superAdminPassword) {
        validateSuperAdminPassword(superAdminPassword);

        CompanyAccount company = companyAccountRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        // remover dependências antes de excluir a conta
        companyActivitySectorsRepository.deleteByCompany(company);
        companyAccountOwnerRepository.deleteByCompanyAccount(company);
        companyAccountRepository.delete(company);
        AdminAccount requester = resolveAuthenticatedAdmin();
        actionLogService.logAdmin(requester, "Rejeitou credencial da empresa " + company.getName());
    }


    private void validateSuperAdminPassword(String rawPassword) {
        String sanitized = rawPassword == null ? "" : rawPassword.trim();
        if (sanitized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password do super admin é obrigatória");
        }

        AdminAccount requester = resolveAuthenticatedAdmin();
        if (requester.getRole() != UserType.SUPERADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente super admins podem aprovar credenciais");
        }

        if (!passwordEncoder.matches(sanitized, requester.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password do super admin inválida");
        }
    }

    private AdminAccount resolveAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação requerida");
        }

        String principal = authentication.getName();
        if (principal == null || !principal.startsWith(ADMIN_TOKEN_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente super admins podem aprovar credenciais");
        }

        String username = principal.substring(ADMIN_TOKEN_PREFIX.length());
        return adminAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta do super admin não encontrada"));
    }
}
