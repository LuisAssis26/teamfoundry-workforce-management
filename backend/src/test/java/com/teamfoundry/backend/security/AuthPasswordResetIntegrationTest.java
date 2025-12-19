package com.teamfoundry.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.auth.model.tokens.PasswordResetToken;
import com.teamfoundry.backend.auth.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthPasswordResetIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EmployeeAccountRepository employeeAccountRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String email = "reset@test.com";
    private final String rawPassword = "oldSecret";

    @BeforeEach
    void setup() {
        passwordResetTokenRepository.deleteAll();
        employeeAccountRepository.deleteAll();
        accountRepository.deleteAll();

        EmployeeAccount acc = new EmployeeAccount();
        acc.setEmail(email);
        acc.setPassword(passwordEncoder.encode(rawPassword));
        acc.setNif(123456789);
        acc.setRole(UserType.EMPLOYEE);
        acc.setVerified(true);
        acc.setRegistrationStatus(RegistrationStatus.COMPLETED);
        employeeAccountRepository.save(acc);
    }

    @Test
    @DisplayName("Forgot password cria token e reset com code funciona")
    void forgotAndResetPasswordFlow() throws Exception {
        var forgotBody = objectMapper.writeValueAsString(Map.of(
                "email", email
        ));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgotBody))
                .andExpect(status().isNoContent());

        // Obter o token gerado para o utilizador
        EmployeeAccount user = employeeAccountRepository.findByEmail(email).orElseThrow();
        PasswordResetToken prt = passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();

        var newPass = "newSecret123";
        var resetBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "code", prt.getToken(),
                "newPassword", newPass
        ));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody))
                .andExpect(status().isNoContent());

        // Verifica password alterada e tokens limpos
        EmployeeAccount updated = employeeAccountRepository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches(newPass, updated.getPassword())).isTrue();
        assertThat(passwordResetTokenRepository.findByUserAndToken(updated, prt.getToken())).isEmpty();
    }
}


