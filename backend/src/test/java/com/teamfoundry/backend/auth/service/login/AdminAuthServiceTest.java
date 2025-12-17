package com.teamfoundry.backend.auth.service.login;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.auth.dto.login.AdminLoginResponse;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminAuthServiceTest {

    @Mock AdminAccountRepository adminAccountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AdminAuthService service;

    @Test
    void authenticate_whenValid_returnsLoginResponse() {
        AdminAccount admin = new AdminAccount(10, "alpha", "hash", UserType.ADMIN, false);
        when(adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse("alpha"))
                .thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateToken("admin:alpha", "ADMIN", 10)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        Optional<AdminLoginResponse> result = service.authenticate("alpha", "secret");

        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo(UserType.ADMIN);
        assertThat(result.get().getAccessToken()).isEqualTo("jwt-token");
        assertThat(result.get().getExpiresInSeconds()).isEqualTo(3600L);
        assertThat(result.get().getRefreshToken()).isNull();
        verify(jwtService).generateToken(eq("admin:alpha"), eq("ADMIN"), eq(10));
    }

    @Test
    void authenticate_whenPasswordDoesNotMatch_returnsEmpty() {
        AdminAccount admin = new AdminAccount(10, "alpha", "hash", UserType.ADMIN, false);
        when(adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse("alpha"))
                .thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        Optional<AdminLoginResponse> result = service.authenticate("alpha", "wrong");

        assertThat(result).isEmpty();
        verify(jwtService, never()).generateToken(any(), any(), anyInt());
    }

    @Test
    void authenticate_whenAdminNotFound_returnsEmpty() {
        when(adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse("missing"))
                .thenReturn(Optional.empty());

        Optional<AdminLoginResponse> result = service.authenticate("missing", "secret");

        assertThat(result).isEmpty();
        verifyNoInteractions(passwordEncoder, jwtService);
    }
}

