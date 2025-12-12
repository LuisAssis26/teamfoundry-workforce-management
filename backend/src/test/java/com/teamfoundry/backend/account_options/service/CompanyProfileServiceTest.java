package com.teamfoundry.backend.account_options.service;

import com.teamfoundry.backend.account.model.CompanyAccount;
import com.teamfoundry.backend.account.model.CompanyAccountManager;
import com.teamfoundry.backend.account.repository.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.CompanyAccountRepository;
import com.teamfoundry.backend.account.service.VerificationEmailService;
import com.teamfoundry.backend.account_options.dto.company.CompanyManagerUpdateRequest;
import com.teamfoundry.backend.account_options.dto.company.CompanyManagerVerifyRequest;
import com.teamfoundry.backend.account_options.dto.company.CompanyProfileResponse;
import com.teamfoundry.backend.security.model.AuthToken;
import com.teamfoundry.backend.security.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CompanyProfileServiceTest {

    @Mock CompanyAccountRepository companyAccountRepository;
    @Mock CompanyAccountOwnerRepository ownerRepository;
    @Mock AuthTokenRepository authTokenRepository;
    @Mock VerificationEmailService verificationEmailService;

    @InjectMocks CompanyProfileService service;

    private CompanyAccount company;

    @BeforeEach
    void init() {
        company = new CompanyAccount();
        company.setId(1);
        company.setEmail("company@test.com");
        company.setName("ACME");
        company.setNif(123456789);
        company.setAddress("Rua 1");
        company.setCountry("PT");
        company.setPhone("+351900000000");
        company.setWebsite("https://acme.test");
        company.setDescription("Desc");
        company.setStatus(true);
    }

    @Test
    @DisplayName("getProfile devolve dados da empresa e manager")
    void getProfile_returnsCompanyAndManager() {
        CompanyAccountManager manager = new CompanyAccountManager();
        manager.setName("Alice");
        manager.setEmail("alice@test.com");
        manager.setPhone("+351911111111");
        manager.setPosition("CTO");

        when(companyAccountRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));
        when(ownerRepository.findByCompanyAccount_Email("company@test.com")).thenReturn(Optional.of(manager));

        CompanyProfileResponse res = service.getProfile("company@test.com");

        assertThat(res.getEmail()).isEqualTo("company@test.com");
        assertThat(res.getManager().getEmail()).isEqualTo("alice@test.com");
        verify(companyAccountRepository).findByEmail("company@test.com");
    }

    @Test
    @DisplayName("updateManager atualiza nome/phone/position do responsavel")
    void updateManager_updatesManagerFields() {
        CompanyAccountManager existing = new CompanyAccountManager();
        existing.setEmail("old@test.com");

        when(companyAccountRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));
        when(ownerRepository.findByCompanyAccount_Email("company@test.com")).thenReturn(Optional.of(existing));
        when(ownerRepository.save(any(CompanyAccountManager.class))).thenAnswer(inv -> inv.getArgument(0));

        CompanyManagerUpdateRequest req = new CompanyManagerUpdateRequest();
        req.setName("Novo");
        req.setPhone("+351922222222");
        req.setPosition("COO");

        CompanyProfileResponse res = service.updateManager("company@test.com", req);

        assertThat(res.getManager().getName()).isEqualTo("Novo");
        assertThat(existing.getPhone()).isEqualTo("+351922222222");
        verify(ownerRepository).save(existing);
    }

    @Test
    @DisplayName("sendVerificationCode cria token e envia email normalizado")
    void sendVerificationCode_createsTokenAndSendsEmail() {
        when(companyAccountRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sendVerificationCode("company@test.com", "NEW@MAIL.COM");

        verify(authTokenRepository).deleteAllByUser(company);
        ArgumentCaptor<AuthToken> captor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(captor.capture());
        AuthToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(company);
        assertThat(saved.getToken()).hasSize(6);
        verify(verificationEmailService).sendVerificationCode("new@mail.com", saved.getToken());
    }

    @Test
    @DisplayName("verifyAndUpdateManager aplica email/campos se codigo valido e nao expirado")
    void verifyAndUpdateManager_withValidCode_updatesManager() {
        CompanyAccountManager existing = new CompanyAccountManager();
        existing.setEmail("old@test.com");

        AuthToken token = new AuthToken();
        token.setToken("123456");
        token.setUser(company);
        token.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60)));
        token.setExpireAt(Timestamp.from(Instant.now().plusSeconds(300)));

        when(companyAccountRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));
        when(ownerRepository.findByCompanyAccount_Email("company@test.com")).thenReturn(Optional.of(existing));
        when(authTokenRepository.findByAccountAndCode(company, "123456")).thenReturn(Optional.of(token));

        CompanyManagerVerifyRequest req = new CompanyManagerVerifyRequest();
        req.setNewEmail("novo@test.com");
        req.setCode("123456");
        req.setName("Novo Nome");
        req.setPhone("+351933333333");
        req.setPosition("COO");

        CompanyProfileResponse res = service.verifyAndUpdateManager("company@test.com", req);

        assertThat(res.getManager().getEmail()).isEqualTo("novo@test.com");
        assertThat(existing.getName()).isEqualTo("Novo Nome");
        verify(authTokenRepository).delete(token);
    }

    @Test
    @DisplayName("verifyAndUpdateManager com codigo invalido lança BAD_REQUEST")
    void verifyAndUpdateManager_invalidCode_throws() {
        when(companyAccountRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));
        when(authTokenRepository.findByAccountAndCode(company, "000000")).thenReturn(Optional.empty());

        CompanyManagerVerifyRequest req = new CompanyManagerVerifyRequest();
        req.setNewEmail("novo@test.com");
        req.setCode("000000");
        req.setName("Novo Nome");
        req.setPhone("+351933333333");
        req.setPosition("COO");

        assertThatThrownBy(() -> service.verifyAndUpdateManager("company@test.com", req))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("verifyAndUpdateManager com token expirado lança BAD_REQUEST")
    void verifyAndUpdateManager_expiredToken_throws() {
        AuthToken token = new AuthToken();
        token.setToken("123456");
        token.setUser(company);
        token.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(3600)));
        token.setExpireAt(Timestamp.from(Instant.now().minusSeconds(10)));

        when(companyAccountRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));
        when(authTokenRepository.findByAccountAndCode(company, "123456")).thenReturn(Optional.of(token));

        CompanyManagerVerifyRequest req = new CompanyManagerVerifyRequest();
        req.setNewEmail("novo@test.com");
        req.setCode("123456");
        req.setName("Novo Nome");
        req.setPhone("+351933333333");
        req.setPosition("COO");

        assertThatThrownBy(() -> service.verifyAndUpdateManager("company@test.com", req))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("getProfile sem email lança UNAUTHORIZED")
    void getProfile_withoutEmail_throwsUnauthorized() {
        assertThatThrownBy(() -> service.getProfile(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }
}
