package com.teamfoundry.backend.superadmin.service.credentials;

import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialRequest;
import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialResponse;
import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialUpdateRequest;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import lombok.RequiredArgsConstructor;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import com.teamfoundry.backend.common.service.ActionLogService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.teamfoundry.backend.account.enums.UserType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Camada responsável por listar, criar e atualizar administradores.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCredentialService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRequestRepository teamRequestRepository;
    private final ActionLogService actionLogService;
    private static final String ADMIN_TOKEN_PREFIX = "admin:";

    public List<AdminCredentialResponse> listAdminCredentials() {
        return adminAccountRepository
                .findByDeactivatedFalse(Sort.by(Sort.Direction.ASC, "username"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminCredentialResponse createAdmin(AdminCredentialRequest request) {
        validateSuperAdminPassword(request.superAdminPassword());

        adminAccountRepository.findByUsername(request.username()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username já utilizado");
        });

        AdminAccount admin = new AdminAccount();
        admin.setUsername(request.username());
        admin.setRole(request.role());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setDeactivated(false);

        AdminAccount saved = adminAccountRepository.save(admin);
        actionLogService.logAdmin(resolveAuthenticatedAdmin(), "Criou admin " + saved.getUsername());
        return toResponse(saved);
    }

    @Transactional
    public AdminCredentialResponse updateAdmin(int id, AdminCredentialUpdateRequest request) {
        validateSuperAdminPassword(request.superAdminPassword());

        AdminAccount admin = adminAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

        adminAccountRepository.findByUsername(request.username())
                .filter(existing -> existing.getId() != id)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username já utilizado");
                });

        admin.setUsername(request.username());
        admin.setRole(request.role());

        if (request.password() != null && !request.password().isBlank()) {
            admin.setPassword(passwordEncoder.encode(request.password()));
        }

        AdminAccount saved = adminAccountRepository.save(admin);
        actionLogService.logAdmin(resolveAuthenticatedAdmin(), "Atualizou admin " + saved.getUsername());
        return toResponse(saved);
    }

    private void validateSuperAdminPassword(String rawPassword) {
        String sanitized = rawPassword == null ? "" : rawPassword.trim();
        if (sanitized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password do super admin é obrigatória");
        }

        AdminAccount requester = resolveAuthenticatedAdmin();
        if (requester.getRole() != UserType.SUPERADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente super admins podem alterar credenciais");
        }

        if (!passwordEncoder.matches(sanitized, requester.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password do super admin inválida");
        }
    }

    @Transactional
    public void disableAdmin(int id, String superAdminPassword) {
        validateSuperAdminPassword(superAdminPassword);

        AdminAccount requester = resolveAuthenticatedAdmin();
        AdminAccount admin = adminAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));

        if (admin.getId() == requester.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível desativar a própria conta.");
        }

        if (admin.getRole() == UserType.SUPERADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é permitido desativar contas de super admin.");
        }

        long assigned = teamRequestRepository.countByResponsibleAdminId(admin.getId());
        if (assigned > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin possui requisições associadas. Reatribua-as antes de desativar.");
        }

        admin.setDeactivated(true);
        adminAccountRepository.save(admin);
        actionLogService.logAdmin(requester, "Desativou admin " + admin.getUsername());
    }



    private AdminAccount resolveAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação requerida");
        }

        String principal = authentication.getName();
        if (principal == null || !principal.startsWith(ADMIN_TOKEN_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente super admins podem alterar credenciais");
        }

        String username = principal.substring(ADMIN_TOKEN_PREFIX.length());
        return adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta do super admin não encontrada"));
    }

    private AdminCredentialResponse toResponse(AdminAccount admin) {
        return new AdminCredentialResponse(admin.getId(), admin.getUsername(), admin.getRole());
    }
}
