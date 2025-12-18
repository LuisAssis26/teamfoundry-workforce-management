package com.teamfoundry.backend.superadmin.controller.credentials;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AdminCredentialController integration")
@Transactional
class AdminCredentialControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminAccountRepository adminAccountRepository;
    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired TeamRequestRepository teamRequestRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String superUsername = "superadmin";
    private final String superPassword = "superSecret!";
    private final String adminPassword = "adminPass!";

    @BeforeEach
    void setup() {
        teamRequestRepository.deleteAll();
        companyAccountRepository.deleteAll();
        adminAccountRepository.deleteAll();

        adminAccountRepository.save(new AdminAccount(0, superUsername,
                passwordEncoder.encode(superPassword), UserType.SUPERADMIN, false));
        adminAccountRepository.save(new AdminAccount(0, "admin",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));
    }

    @Test
    @DisplayName("GET /api/super-admin/credentials/admins devolve admins ativos ordenados")
    void listAdminsReturnsActiveAdminsSorted() throws Exception {
        adminAccountRepository.save(new AdminAccount(0, "beta-admin",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));
        adminAccountRepository.save(new AdminAccount(0, "alpha-admin",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));

        String token = login(superUsername, superPassword);

        mockMvc.perform(get("/api/super-admin/credentials/admins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].username", contains(
                        "admin", "alpha-admin", "beta-admin", "superadmin"
                )));
    }

    @Test
    @DisplayName("POST /admins cria novo admin quando super admin autentica e envia password correta")
    void createAdminWithValidPasswordCreatesAdmin() throws Exception {
        String token = login(superUsername, superPassword);
        Map<String, Object> payload = Map.of(
                "username", "newadmin",
                "password", "NewAdmin#1",
                "role", "ADMIN",
                "superAdminPassword", superPassword
        );

        mockMvc.perform(post("/api/super-admin/credentials/admins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        Optional<AdminAccount> created = adminAccountRepository.findByUsername("newadmin");
        assertThat(created).isPresent();
        assertThat(passwordEncoder.matches("NewAdmin#1", created.get().getPassword())).isTrue();
    }

    @Test
    @DisplayName("PUT /admins atualiza username, role e password quando autenticado")
    void updateAdminUpdatesFields() throws Exception {
        String token = login(superUsername, superPassword);
        AdminAccount existing = adminAccountRepository.findByUsername("admin").orElseThrow();

        Map<String, Object> payload = Map.of(
                "username", "admin-updated",
                "role", "ADMIN",
                "password", "Updated#123",
                "superAdminPassword", superPassword
        );

        mockMvc.perform(put("/api/super-admin/credentials/admins/{id}", existing.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin-updated"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        AdminAccount updated = adminAccountRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("admin-updated");
        assertThat(passwordEncoder.matches("Updated#123", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Admin comum nao consegue criar administradores")
    void createAdminWithAdminTokenReturnsForbidden() throws Exception {
        String adminToken = login("admin", adminPassword);
        Map<String, Object> payload = Map.of(
                "username", "forbidden",
                "password", "Admin#1234",
                "role", "ADMIN",
                "superAdminPassword", adminPassword
        );

        mockMvc.perform(post("/api/super-admin/credentials/admins")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /admins/{id} falha se admin tem requisicoes atribuidas")
    void disableAdminWithAssignmentsReturnsBadRequest() throws Exception {
        String token = login(superUsername, superPassword);
        AdminAccount assigned = adminAccountRepository.save(new AdminAccount(0, "assigned",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));
        CompanyAccount company = createCompany("pending@test.com", 555222111);

        TeamRequest teamRequest = new TeamRequest();
        teamRequest.setCompany(company);
        teamRequest.setTeamName("Support Squad");
        teamRequest.setState(State.INCOMPLETE);
        teamRequest.setResponsibleAdminId(assigned.getId());
        teamRequest.setCreatedAt(LocalDateTime.now());
        teamRequestRepository.save(teamRequest);

        mockMvc.perform(delete("/api/super-admin/credentials/admins/{id}", assigned.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "superAdminPassword", superPassword
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admins/{id} desativa admin sem atribuicoes")
    void disableAdminWithoutAssignmentsDeactivatesAdmin() throws Exception {
        String token = login(superUsername, superPassword);
        AdminAccount target = adminAccountRepository.save(new AdminAccount(0, "to-disable",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));

        mockMvc.perform(delete("/api/super-admin/credentials/admins/{id}", target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "superAdminPassword", superPassword
                        ))))
                .andExpect(status().isNoContent());

        AdminAccount deactivated = adminAccountRepository.findById(target.getId()).orElseThrow();
        assertThat(deactivated.isDeactivated()).isTrue();
    }

    private String login(String username, String password) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        var response = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private CompanyAccount createCompany(String email, int nif) {
        CompanyAccount company = new CompanyAccount();
        company.setEmail(email);
        company.setPassword(passwordEncoder.encode("companyPass"));
        company.setNif(nif);
        company.setRole(UserType.COMPANY);
        company.setRegistrationStatus(RegistrationStatus.COMPLETED);
        company.setName("Company " + nif);
        company.setAddress("Rua Central " + nif);
        company.setCountry("Portugal");
        company.setStatus(false);
        return companyAccountRepository.save(company);
    }
}

