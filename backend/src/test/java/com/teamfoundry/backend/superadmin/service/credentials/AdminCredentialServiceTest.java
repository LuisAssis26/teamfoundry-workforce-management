package com.teamfoundry.backend.superadmin.service.credentials;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.common.service.ActionLogService;
import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialRequest;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminCredentialServiceTest {

    @Mock AdminAccountRepository adminAccountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TeamRequestRepository teamRequestRepository;
    @Mock ActionLogService actionLogService;

    @InjectMocks AdminCredentialService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAdminWithMissingSuperAdminPasswordThrowsBadRequest() {
        AdminCredentialRequest request = new AdminCredentialRequest(
                "newadmin", "Password#1", UserType.ADMIN, "   "
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAdmin(request));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createAdminWithNonSuperAdminUserThrowsForbidden() {
        AdminAccount admin = new AdminAccount(2, "admin", "hash", UserType.ADMIN, false);
        authenticate(admin);

        AdminCredentialRequest request = new AdminCredentialRequest(
                "newadmin", "Password#1", UserType.ADMIN, "secret"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createAdmin(request));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(adminAccountRepository, never()).save(any());
    }

    @Test
    void disableAdminWhenHasAssignmentsThrowsBadRequest() {
        AdminAccount superAdmin = new AdminAccount(1, "superadmin", "encoded", UserType.SUPERADMIN, false);
        authenticate(superAdmin);
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        AdminAccount target = new AdminAccount(2, "target", "pwd", UserType.ADMIN, false);
        when(adminAccountRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(teamRequestRepository.countByResponsibleAdminId(target.getId())).thenReturn(2L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.disableAdmin(target.getId(), "secret"));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(adminAccountRepository, never()).save(any());
    }

    @Test
    void disableAdminWithoutAssignmentsMarksAsDeactivated() {
        AdminAccount superAdmin = new AdminAccount(1, "superadmin", "encoded", UserType.SUPERADMIN, false);
        authenticate(superAdmin);
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        AdminAccount target = new AdminAccount(3, "to-disable", "pwd", UserType.ADMIN, false);
        when(adminAccountRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(teamRequestRepository.countByResponsibleAdminId(target.getId())).thenReturn(0L);
        when(adminAccountRepository.save(any(AdminAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.disableAdmin(target.getId(), "secret");

        ArgumentCaptor<AdminAccount> captor = ArgumentCaptor.forClass(AdminAccount.class);
        verify(adminAccountRepository).save(captor.capture());
        assertThat(captor.getValue().isDeactivated()).isTrue();
        verify(actionLogService).logAdmin(eq(superAdmin), any());
    }

    private void authenticate(AdminAccount admin) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin:" + admin.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse(admin.getUsername()))
                .thenReturn(Optional.of(admin));
    }
}

