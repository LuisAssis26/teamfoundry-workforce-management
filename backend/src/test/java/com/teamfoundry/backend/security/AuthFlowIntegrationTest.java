package com.teamfoundry.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Flow")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EmployeeAccountRepository employeeAccountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    private final String email = "user@test.com";
    private final String rawPassword = "secret";

    @BeforeEach
    void setupUser() {
        employeeAccountRepository.deleteAll();
        accountRepository.deleteAll();
        employeeAccountRepository.save(buildEmployee(email, true));
    }

    @Test
    @DisplayName("Login success → devolve role e mensagem")
    void loginSuccessReturnsRoleAndMessage() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", rawPassword
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("EMPLOYEE"))
                .andExpect(jsonPath("$.message").value("Login efetuado com sucesso"));
    }

    @Test
    @DisplayName("Login inválido → 401 Unauthorized")
    void loginInvalidCredentialsUnauthorized() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "wrong"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Conta inativa → devolve 401 com mensagem de conta não verificada")
    void loginInactiveAccountReturnsUnauthorized() throws Exception {
        employeeAccountRepository.save(buildEmployee("inactive@test.com", false));

        var body = objectMapper.writeValueAsString(Map.of(
                "email", "inactive@test.com",
                "password", rawPassword
        ));

        var response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("Conta ainda não foi verificada");
    }

    private EmployeeAccount buildEmployee(String email, boolean active) {
        EmployeeAccount account = new EmployeeAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setNif(active ? 123456789 : 987654321);
        account.setRole(UserType.EMPLOYEE);
        account.setVerified(active);
        account.setRegistrationStatus(active ? RegistrationStatus.COMPLETED : RegistrationStatus.PENDING);
        return account;
    }
}

