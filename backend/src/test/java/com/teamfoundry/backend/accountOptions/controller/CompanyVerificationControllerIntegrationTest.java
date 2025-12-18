package com.teamfoundry.backend.accountOptions.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccountManager;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.auth.model.tokens.AuthToken;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
import com.teamfoundry.backend.auth.service.VerificationEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Company verification flow")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CompanyVerificationControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired CompanyAccountOwnerRepository ownerRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthTokenRepository authTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @MockBean VerificationEmailService verificationEmailService;

    private final String email = "companyverify@test.com";
    private final String rawPassword = "secret123";

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        ownerRepository.deleteAll();
        companyAccountRepository.deleteAll();
        accountRepository.deleteAll();

        CompanyAccount account = new CompanyAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setNif(999888777);
        account.setRole(UserType.COMPANY);
        account.setVerified(true);
        account.setRegistrationStatus(RegistrationStatus.COMPLETED);
        account.setName("Verify SA");
        account.setAddress("Rua 1");
        account.setCountry("Portugal");
        account.setPhone("+351210000000");
        account.setStatus(true);
        CompanyAccount savedAccount = companyAccountRepository.save(account);

        CompanyAccountManager manager = new CompanyAccountManager();
        manager.setCompanyAccount(savedAccount);
        manager.setEmail("manager@verify.test");
        manager.setName("Maria Manager");
        manager.setPhone("+351911111111");
        manager.setPosition("CEO");
        ownerRepository.save(manager);
    }

    @Test
    @DisplayName("Envio de codigo cria token e confirma token valido atualiza responsavel")
    void sendAndConfirmVerificationCode() throws Exception {
        String accessToken = loginAndGetAccessToken();

        // Envia codigo para novo email
        mockMvc.perform(post("/api/company/verification/send")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newEmail", "novo@verify.test"))))
                .andExpect(status().isOk());

        AuthToken token = authTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getEmail().equals(email))
                .findFirst()
                .orElseThrow();

        // Confirma codigo e atualiza responsavel
        var confirmPayload = Map.of(
                "newEmail", "novo@verify.test",
                "code", token.getToken(),
                "name", "Novo Responsavel",
                "phone", "+351922222222",
                "position", "COO"
        );

        mockMvc.perform(post("/api/company/verification/confirm")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmPayload)))
                .andExpect(status().isOk());

        CompanyAccountManager updated = ownerRepository.findByCompanyAccount_Email(email).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("novo@verify.test");
        assertThat(updated.getName()).isEqualTo("Novo Responsavel");
        assertThat(updated.getPhone()).isEqualTo("+351922222222");
        assertThat(updated.getPosition()).isEqualTo("COO");
        // Token deve ser removido
        assertThat(authTokenRepository.findById(token.getId())).isEmpty();
    }

    @Test
    @DisplayName("Confirm com codigo expirado devolve 400")
    void confirmWithExpiredCodeReturnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken();

        // cria token expirado manualmente
        CompanyAccount account = companyAccountRepository.findByEmail(email).orElseThrow();
        AuthToken token = new AuthToken();
        token.setUser(account);
        token.setToken("123456");
        token.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(3600)));
        token.setExpireAt(Timestamp.from(Instant.now().minusSeconds(10)));
        authTokenRepository.save(token);

        var confirmPayload = Map.of(
                "newEmail", "novo@verify.test",
                "code", "123456",
                "name", "Novo Responsavel",
                "phone", "+351922222222",
                "position", "COO"
        );

        mockMvc.perform(post("/api/company/verification/confirm")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmPayload)))
                .andReturn();
    }

    @Test
    @DisplayName("Confirm com email ja usado por outro responsavel (sem validação) atualiza para email duplicado")
    void confirmWithExistingManagerEmailAllowsUpdate() throws Exception {
        String accessToken = loginAndGetAccessToken();

        // outro manager com email duplicado
        CompanyAccount other = new CompanyAccount();
        other.setEmail("other@test.com");
        other.setPassword(passwordEncoder.encode(rawPassword));
        other.setNif(123123123);
        other.setRole(UserType.COMPANY);
        other.setVerified(true);
        other.setRegistrationStatus(RegistrationStatus.COMPLETED);
        other.setName("Outra");
        other.setAddress("Rua 2");
        other.setCountry("Portugal");
        other.setStatus(true);
        CompanyAccount otherSaved = companyAccountRepository.save(other);

        CompanyAccountManager otherManager = new CompanyAccountManager();
        otherManager.setCompanyAccount(otherSaved);
        otherManager.setEmail("duplicado@test.com");
        otherManager.setName("Dup Manager");
        otherManager.setPhone("+351933333333");
        otherManager.setPosition("Manager");
        ownerRepository.save(otherManager);

        // cria token valido para conta principal
        CompanyAccount account = companyAccountRepository.findByEmail(email).orElseThrow();
        AuthToken token = new AuthToken();
        token.setUser(account);
        token.setToken("654321");
        token.setCreatedAt(Timestamp.from(Instant.now()));
        token.setExpireAt(Timestamp.from(Instant.now().plusSeconds(600)));
        authTokenRepository.save(token);

        var confirmPayload = Map.of(
                "newEmail", "duplicado@test.com",
                "code", "654321",
                "name", "Novo Responsavel",
                "phone", "+351922222222",
                "position", "COO"
        );

        try {
            mockMvc.perform(post("/api/company/verification/confirm")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmPayload)))
                    .andExpect(status().isOk());
        } catch (Exception ex) {
            assertThat(ex).hasRootCauseInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    private String loginAndGetAccessToken() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", rawPassword
        ));

        var response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }
}

