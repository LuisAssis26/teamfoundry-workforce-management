package com.teamfoundry.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import org.junit.jupiter.api.BeforeEach;
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
class AuthLoginRolesIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmployeeAccountRepository employeeAccountRepository;

    @Autowired
    CompanyAccountRepository companyAccountRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final String rawPassword = "secret";

    @BeforeEach
    void cleanTables() {
        companyAccountRepository.deleteAll();
        employeeAccountRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void login_candidate_returns_access_token_and_role_employee() throws Exception {
        employeeAccountRepository.save(buildEmployee("candidate@test.com", true));

        var body = objectMapper.writeValueAsString(Map.of(
                "email", "candidate@test.com",
                "password", rawPassword
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("EMPLOYEE"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    @Test
    void login_company_returns_access_token_and_role_company() throws Exception {
        companyAccountRepository.save(buildCompany("company@test.com", true));

        var body = objectMapper.writeValueAsString(Map.of(
                "email", "company@test.com",
                "password", rawPassword
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("COMPANY"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    private EmployeeAccount buildEmployee(String email, boolean active) {
        EmployeeAccount account = new EmployeeAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setNif(123456789);
        account.setRole(UserType.EMPLOYEE);
        account.setVerified(active);
        account.setRegistrationStatus(active ? RegistrationStatus.COMPLETED : RegistrationStatus.PENDING);
        return account;
    }

    private CompanyAccount buildCompany(String email, boolean active) {
        CompanyAccount account = new CompanyAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setNif(987654321);
        account.setRole(UserType.COMPANY);
        account.setVerified(active);
        account.setRegistrationStatus(active ? RegistrationStatus.COMPLETED : RegistrationStatus.PENDING);
        account.setName("ACME Corp");
        account.setAddress("Rua Principal 123");
        account.setCountry("PT");
        account.setStatus(true);
        return account;
    }
}
