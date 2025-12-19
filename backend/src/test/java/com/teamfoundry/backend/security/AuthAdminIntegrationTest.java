package com.teamfoundry.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthAdminIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminAccountRepository adminAccountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    private final String rawPassword = "adminSecret";

    @BeforeEach
    void setupAdmins() {
        adminAccountRepository.deleteAll();
        adminAccountRepository.save(new AdminAccount(0, "admin", passwordEncoder.encode(rawPassword), UserType.ADMIN, false));
        adminAccountRepository.save(new AdminAccount(0, "superadmin", passwordEncoder.encode(rawPassword), UserType.SUPERADMIN, false));
    }

    @Test
    @DisplayName("Login de admin retorna token e role ADMIN")
    void adminLoginReturnsTokenAndRole() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", "admin",
                "password", rawPassword
        ));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    @Test
    @DisplayName("Login de superadmin retorna token e role SUPERADMIN")
    void superadminLoginReturnsTokenAndRole() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", "superadmin",
                "password", rawPassword
        ));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPERADMIN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    @Test
    @DisplayName("Credenciais erradas devolvem 401 com erro padrao")
    void loginWrongPasswordReturnsUnauthorized() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", "admin",
                "password", "wrong"
        ));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }
}

