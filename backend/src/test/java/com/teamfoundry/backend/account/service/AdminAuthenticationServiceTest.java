package com.teamfoundry.backend.account.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.teamfoundry.backend.auth.service.login.AdminAuthService;
import com.teamfoundry.backend.auth.dto.login.AdminLoginResponse;
import com.teamfoundry.backend.auth.service.login.JwtService;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do serviço de autenticação de administradores.
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthenticationServiceTest {

    @Mock
    private AdminAccountRepository adminAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminAuthService adminAuthenticationService;

    @Mock
    private JwtService jwtService;


    @Test
    @DisplayName("authenticate devolve AdminLoginResponse quando credenciais são válidas")
    void authenticateReturnsResponseWhenPasswordMatches() {
        AdminAccount account = new AdminAccount(1, "admin", "hash", UserType.ADMIN, false);
        when(adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse("ADMIN")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateToken("admin:admin", "ADMIN", 1)).thenReturn("access-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        Optional<AdminLoginResponse> result = adminAuthenticationService.authenticate("ADMIN", "secret");

        assertThat(result).isPresent();
        AdminLoginResponse response = result.get();
        assertThat(response.getRole()).isEqualTo(UserType.ADMIN);
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getExpiresInSeconds()).isEqualTo(3600L);
    }


    @Test
    @DisplayName("authenticate devolve vazio quando o hash não corresponde")
    void authenticateReturnsEmptyWhenPasswordDoesNotMatch() {
        AdminAccount account = new AdminAccount(1, "admin", "hash", UserType.ADMIN, false);
        when(adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse("admin")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        Optional<AdminLoginResponse> result = adminAuthenticationService.authenticate("admin", "wrong");
        assertThat(result).isEmpty();

        assertThat(result).isEmpty();
    }
}
