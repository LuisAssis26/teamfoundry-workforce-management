package com.teamfoundry.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRememberMeIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EmployeeAccountRepository employeeAccountRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthTokenRepository authTokenRepository;

    private final String email = "remember@test.com";
    private final String rawPassword = "secret";

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        employeeAccountRepository.deleteAll();
        accountRepository.deleteAll();

        EmployeeAccount account = new EmployeeAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setNif(111222333);
        account.setRole(UserType.EMPLOYEE);
        account.setVerified(true);
        account.setRegistrationStatus(RegistrationStatus.COMPLETED);
        employeeAccountRepository.save(account);
    }

    @Test
    @DisplayName("Login com remember-me emite refresh_token cookie e permite refresh")
    void loginWithRememberSetsCookieAndAllowsRefresh() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", rawPassword,
                "rememberMe", true
        ));

        var mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String setCookie = mvcResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refresh_token=");
        assertThat(setCookie).doesNotContain("Max-Age=0");

        // Extrai valor do refresh_token
        String refreshValue = null;
        for (String part : setCookie.split(";")) {
            if (part.trim().startsWith("refresh_token=")) {
                refreshValue = part.trim().substring("refresh_token=".length());
                break;
            }
        }
        assertThat(refreshValue).isNotNull();

        // Usa o cookie para pedir refresh
        var refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Confere que o payload tem expiresInSeconds coerente
        JsonNode json = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        assertThat(json.get("expiresInSeconds").asLong()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Login sem remember limpa o refresh_token cookie")
    void loginWithoutRememberClearsCookie() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", rawPassword,
                "rememberMe", false
        ));

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        // Pode ser null (sem header) ou um cookie expirado; se existir, deve expirar imediatamente
        if (setCookie != null) {
            assertThat(setCookie).contains("refresh_token=");
            assertThat(setCookie).contains("Max-Age=0");
        }
    }
}


