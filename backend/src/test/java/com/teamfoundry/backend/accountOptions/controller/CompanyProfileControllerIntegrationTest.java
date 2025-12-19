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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CompanyProfileController integration")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Transactional
class CompanyProfileControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired CompanyAccountOwnerRepository ownerRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthTokenRepository authTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String email = "company@test.com";
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
        account.setNif(555666777);
        account.setRole(UserType.COMPANY);
        account.setVerified(true);
        account.setRegistrationStatus(RegistrationStatus.COMPLETED);
        account.setName("ACME Lda");
        account.setAddress("Rua Principal 123");
        account.setCountry("Portugal");
        account.setPhone("+351211000000");
        account.setWebsite("https://acme.test");
        account.setDescription("Empresa de teste");
        account.setStatus(true);
        CompanyAccount savedAccount = companyAccountRepository.save(account);

        CompanyAccountManager manager = new CompanyAccountManager();
        manager.setCompanyAccount(savedAccount);
        manager.setEmail("owner@acme.test");
        manager.setName("Alice Manager");
        manager.setPhone("+351933333333");
        manager.setPosition("CTO");
        ownerRepository.save(manager);
    }

    @Test
    @DisplayName("GET /api/company/profile devolve dados da empresa e responsavel")
    void getProfileReturnsCompanyAndManagerData() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(get("/api/company/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ACME Lda"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nif").value(555666777))
                .andExpect(jsonPath("$.address").value("Rua Principal 123"))
                .andExpect(jsonPath("$.country").value("Portugal"))
                .andExpect(jsonPath("$.manager.name").value("Alice Manager"))
                .andExpect(jsonPath("$.manager.email").value("owner@acme.test"))
                .andExpect(jsonPath("$.manager.phone").value("+351933333333"))
                .andExpect(jsonPath("$.manager.position").value("CTO"));
    }

    @Test
    @DisplayName("PUT /api/company/profile atualiza o responsavel e persiste alteracoes")
    void updateProfileUpdatesManager() throws Exception {
        String accessToken = loginAndGetAccessToken();

        Map<String, Object> payload = Map.of(
                "name", "Bob Lead",
                "phone", "+351944444444",
                "position", "COO"
        );

        mockMvc.perform(put("/api/company/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manager.name").value("Bob Lead"))
                .andExpect(jsonPath("$.manager.phone").value("+351944444444"))
                .andExpect(jsonPath("$.manager.position").value("COO"))
                .andExpect(jsonPath("$.manager.email").value("owner@acme.test"));

        Optional<CompanyAccountManager> manager = ownerRepository.findByCompanyAccount_Email(email);
        assertThat(manager).isPresent();
        assertThat(manager.get().getName()).isEqualTo("Bob Lead");
        assertThat(manager.get().getPhone()).isEqualTo("+351944444444");
        assertThat(manager.get().getPosition()).isEqualTo("COO");
    }

    @Test
    @DisplayName("Endpoints de perfil da empresa sem token devolvem 401 Unauthorized")
    void profileEndpointsWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/company/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/company/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "X",
                                "phone", "123",
                                "position", "Y"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT profile da empresa com payload invalido devolve 400")
    void updateProfileWithInvalidPayloadReturnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken();

        Map<String, Object> payload = Map.of(
                "name", "",      // obrigatorio
                "phone", "123",  // muito curto
                "position", ""   // obrigatorio
        );

        mockMvc.perform(put("/api/company/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
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

