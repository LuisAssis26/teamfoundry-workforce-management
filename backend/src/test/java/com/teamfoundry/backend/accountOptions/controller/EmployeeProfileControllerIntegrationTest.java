package com.teamfoundry.backend.accountOptions.controller;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.time.LocalDate;
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
@DisplayName("EmployeeProfileController integration")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EmployeeProfileControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired com.teamfoundry.backend.security.service.JwtService jwtService;

    @Autowired EmployeeAccountRepository employeeAccountRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String email = "profile@test.com";
    private final String rawPassword = "secret123";

    @BeforeEach
    void setupUser() {
        employeeAccountRepository.deleteAll();
        accountRepository.deleteAll();

        EmployeeAccount account = new EmployeeAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setNif(123456789);
        account.setRole(UserType.EMPLOYEE);
        account.setVerified(true);
        account.setRegistrationStatus(RegistrationStatus.COMPLETED);
        account.setName("Joao");
        account.setSurname("Silva");
        account.setGender("MALE");
        account.setBirthDate(LocalDate.of(1990, 6, 15));
        account.setNationality("Portugal");
        account.setPhone("+351912345678");
        employeeAccountRepository.save(account);
    }

    @Test
    @DisplayName("GET /api/employee/profile devolve dados do perfil autenticado")
    void getProfile_returnsAuthenticatedProfile() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(get("/api/employee/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Joao"))
                .andExpect(jsonPath("$.lastName").value("Silva"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.birthDate").value("1990-06-15"))
                .andExpect(jsonPath("$.nationality").value("Portugal"))
                .andExpect(jsonPath("$.nif").value(123456789))
                .andExpect(jsonPath("$.phone").value("+351912345678"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("PUT /api/employee/profile atualiza dados e persiste alteraÇõÇœes")
    void updateProfile_updatesAndPersists() throws Exception {
        String accessToken = loginAndGetAccessToken();

        Map<String, Object> payload = Map.of(
                "firstName", "Maria",
                "lastName", "Sousa",
                "gender", "female",
                "birthDate", "1995-05-10",
                "nationality", "Portugal",
                "nif", 987654321,
                "phone", "+351999888777"
        );

        mockMvc.perform(put("/api/employee/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maria"))
                .andExpect(jsonPath("$.lastName").value("Sousa"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.birthDate").value("1995-05-10"))
                .andExpect(jsonPath("$.nif").value(987654321))
                .andExpect(jsonPath("$.phone").value("+351999888777"));

        Optional<EmployeeAccount> updated = employeeAccountRepository.findByEmail(email.toLowerCase());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Maria");
        assertThat(updated.get().getSurname()).isEqualTo("Sousa");
        assertThat(updated.get().getGender()).isEqualTo("FEMALE");
        assertThat(updated.get().getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 10));
        assertThat(updated.get().getNif()).isEqualTo(987654321);
        assertThat(updated.get().getPhone()).isEqualTo("+351999888777");
    }

    @Test
    @DisplayName("Endpoints de perfil sem token devolvem 401 Unauthorized")
    void profileEndpoints_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employee/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/employee/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Ana",
                                "lastName", "Silva",
                                "gender", "male",
                                "birthDate", "1990-01-01",
                                "nationality", "PT",
                                "nif", 111222333,
                                "phone", "+351910000000"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT profile com payload invalido devolve 400 Bad Request")
    void updateProfile_withInvalidPayload_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken();

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("firstName", "A"); // curto demais
        payload.put("lastName", "");   // obrigat¢rio
        payload.put("gender", "invalid");
        payload.put("birthDate", "2030-01-01"); // futuro
        payload.put("nationality", "");
        // nif omitido -> @NotNull deve falhar
        payload.put("phone", "abc");

        mockMvc.perform(put("/api/employee/profile")
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
