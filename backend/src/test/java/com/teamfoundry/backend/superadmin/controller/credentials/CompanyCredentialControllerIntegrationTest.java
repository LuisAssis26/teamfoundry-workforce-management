package com.teamfoundry.backend.superadmin.controller.credentials;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccountManager;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyActivitySectorsRepository;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("CompanyCredentialController integration")
@Transactional
class CompanyCredentialControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AdminAccountRepository adminAccountRepository;
    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired CompanyAccountOwnerRepository ownerRepository;
    @Autowired CompanyActivitySectorsRepository companyActivitySectorsRepository;
    @Autowired AuthTokenRepository authTokenRepository;

    private final String superPassword = "superSecret!";
    private final String adminPassword = "adminPass!";

    private CompanyAccount firstPending;
    private CompanyAccount secondPending;

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        adminAccountRepository.deleteAll();
        adminAccountRepository.flush();
        companyActivitySectorsRepository.deleteAll();
        ownerRepository.deleteAll();
        companyAccountRepository.deleteAll();

        adminAccountRepository.save(new AdminAccount(0, "superadmin",
                passwordEncoder.encode(superPassword), UserType.SUPERADMIN, false));
        adminAccountRepository.save(new AdminAccount(0, "admin",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));

        firstPending = createPendingCompany("ACME Lda", "acme@test.com", 123456700);
        saveManager(firstPending, "Owner One", "owner1@acme.test");

        secondPending = createPendingCompany("Beta Corp", "beta@test.com", 123456701);
        saveManager(secondPending, "Owner Two", "owner2@beta.test");

        CompanyAccount approved = createPendingCompany("Approved Inc", "approved@test.com", 123456702);
        approved.setStatus(true);
        companyAccountRepository.save(approved);
    }

    @Test
    @DisplayName("GET /companies lista apenas credenciais pendentes")
    void listPendingCompaniesReturnsOnlyPending() throws Exception {
        String token = login(superPassword);

        mockMvc.perform(get("/api/super-admin/credentials/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].companyName").value("Beta Corp"))
                .andExpect(jsonPath("$[0].responsibleEmail").value("owner2@beta.test"))
                .andExpect(jsonPath("$[1].companyName").value("ACME Lda"))
                .andExpect(jsonPath("$[1].responsibleEmail").value("owner1@acme.test"));
    }

    @Test
    @DisplayName("POST /companies/{id}/approve marca empresa como aprovada")
    void approveCompanySetsStatusTrue() throws Exception {
        String token = login(superPassword);

        mockMvc.perform(post("/api/super-admin/credentials/companies/{id}/approve", firstPending.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "superAdminPassword", superPassword
                        ))))
                .andExpect(status().isNoContent());

        CompanyAccount updated = companyAccountRepository.findById(firstPending.getId()).orElseThrow();
        assertThat(updated.isStatus()).isTrue();
    }

    @Test
    @DisplayName("POST /companies/{id}/reject remove credencial pendente e respetivo responsavel")
    void rejectCompanyRemovesCompanyAndManager() throws Exception {
        String token = login(superPassword);

        mockMvc.perform(post("/api/super-admin/credentials/companies/{id}/reject", secondPending.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "superAdminPassword", superPassword
                        ))))
                .andExpect(status().isNoContent());

        assertThat(companyAccountRepository.findById(secondPending.getId())).isEmpty();
        assertThat(ownerRepository.findByCompanyAccount_Email("beta@test.com")).isEmpty();
    }

    @Test
    @DisplayName("Endpoints de credenciais exigem autenticacao")
    void credentialsEndpointsWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/super-admin/credentials/companies"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/super-admin/credentials/companies/{id}/approve", firstPending.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "superAdminPassword", superPassword
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Password errada do super admin devolve 403 no approve")
    void approveCompanyWithWrongPasswordReturnsForbidden() throws Exception {
        String token = login(superPassword);

        mockMvc.perform(post("/api/super-admin/credentials/companies/{id}/approve", firstPending.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "superAdminPassword", "wrong"
                        ))))
                .andExpect(status().isForbidden());
    }

    private String login(String password) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", "superadmin",
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

    private CompanyAccount createPendingCompany(String name, String email, int nif) {
        CompanyAccount company = new CompanyAccount();
        company.setEmail(email);
        company.setPassword(passwordEncoder.encode("companyPass"));
        company.setNif(nif);
        company.setRole(UserType.COMPANY);
        company.setRegistrationStatus(RegistrationStatus.COMPLETED);
        company.setName(name);
        company.setAddress("Rua Central " + nif);
        company.setCountry("Portugal");
        company.setStatus(false);
        return companyAccountRepository.save(company);
    }

    private void saveManager(CompanyAccount company, String name, String email) {
        CompanyAccountManager manager = new CompanyAccountManager();
        manager.setCompanyAccount(company);
        manager.setEmail(email);
        manager.setName(name);
        manager.setPhone("+351911111111");
        manager.setPosition("CTO");
        ownerRepository.save(manager);
    }
}

