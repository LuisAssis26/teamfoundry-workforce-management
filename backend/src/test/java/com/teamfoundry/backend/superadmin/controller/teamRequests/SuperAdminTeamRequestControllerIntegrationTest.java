package com.teamfoundry.backend.superadmin.controller.teamRequests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("SuperAdminTeamRequestController integration")
@Transactional
class SuperAdminTeamRequestControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminAccountRepository adminAccountRepository;
    @Autowired TeamRequestRepository teamRequestRepository;
    @Autowired EmployeeRequestRepository employeeRequestRepository;
    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired AuthTokenRepository authTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String superUsername = "superadmin";
    private final String superPassword = "Super#123";
    private final String adminPassword = "Admin#123";

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        employeeRequestRepository.deleteAll();
        teamRequestRepository.deleteAll();
        companyAccountRepository.deleteAll();
        adminAccountRepository.deleteAll();
        adminAccountRepository.flush();

        adminAccountRepository.save(new AdminAccount(0, superUsername,
                passwordEncoder.encode(superPassword), UserType.SUPERADMIN, false));
        adminAccountRepository.save(new AdminAccount(0, "admin-default",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));
    }

    @Test
    @DisplayName("GET /work-requests devolve lista ordenada e workforceNeeded")
    void listAllReturnsSortedWithWorkforce() throws Exception {
        CompanyAccount company = createCompany("sorted@test.com", 123456789);

        TeamRequest older = createTeamRequest(company, "Alpha Team", State.INCOMPLETE,
                LocalDateTime.now().minusDays(2), null);
        TeamRequest newer = createTeamRequest(company, "Beta Team", State.COMPLETED,
                LocalDateTime.now().minusDays(1), null);

        createEmployeeRequest(older, "Developer");
        createEmployeeRequest(newer, "UX");
        createEmployeeRequest(newer, "PO");

        String token = login(superUsername, superPassword);

        mockMvc.perform(get("/api/super-admin/work-requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(newer.getId()))
                .andExpect(jsonPath("$[0].workforceNeeded").value(2))
                .andExpect(jsonPath("$[1].id").value(older.getId()))
                .andExpect(jsonPath("$[1].workforceNeeded").value(1));
    }

    @Test
    @DisplayName("GET /work-requests/admin-options devolve apenas admins com contagem de atribuicoes")
    void listAdminOptionsReturnsAdminsWithCounts() throws Exception {
        CompanyAccount company = createCompany("count@test.com", 987654321);

        AdminAccount alpha = adminAccountRepository.save(new AdminAccount(0, "alpha-admin",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));
        AdminAccount beta = adminAccountRepository.save(new AdminAccount(0, "beta-admin",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));

        createTeamRequest(company, "One", State.INCOMPLETE, LocalDateTime.now(), alpha.getId());
        createTeamRequest(company, "Two", State.INCOMPLETE, LocalDateTime.now().minusHours(1), beta.getId());
        createTeamRequest(company, "Three", State.INCOMPLETE, LocalDateTime.now().minusHours(2), beta.getId());

        String token = login(superUsername, superPassword);

        mockMvc.perform(get("/api/super-admin/work-requests/admin-options")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username").value("admin-default"))
                .andExpect(jsonPath("$[0].requestCount").value(0))
                .andExpect(jsonPath("$[1].username").value("alpha-admin"))
                .andExpect(jsonPath("$[1].requestCount").value(1))
                .andExpect(jsonPath("$[2].username").value("beta-admin"))
                .andExpect(jsonPath("$[2].requestCount").value(2));
    }

    @Test
    @DisplayName("PATCH /work-requests/{id}/responsible-admin atribui admin comum")
    void assignResponsibleAdminUpdatesRequest() throws Exception {
        CompanyAccount company = createCompany("assign@test.com", 112233445);
        TeamRequest request = createTeamRequest(company, "Needs Owner", State.INCOMPLETE,
                LocalDateTime.now().minusHours(2), null);

        AdminAccount target = adminAccountRepository.save(new AdminAccount(0, "assignable",
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));

        String token = login(superUsername, superPassword);

        mockMvc.perform(patch("/api/super-admin/work-requests/{id}/responsible-admin", request.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("adminId", target.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsibleAdminId").value(target.getId()))
                .andExpect(jsonPath("$.teamName").value("Needs Owner"));

        Integer responsible = teamRequestRepository.findById(request.getId())
                .orElseThrow()
                .getResponsibleAdminId();
        assertThat(responsible).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("PATCH /work-requests/{id}/responsible-admin falha se adminId for superadmin")
    void assignResponsibleAdminWithSuperAdminIdReturnsBadRequest() throws Exception {
        CompanyAccount company = createCompany("bad@test.com", 667788990);
        TeamRequest request = createTeamRequest(company, "Invalid", State.INCOMPLETE,
                LocalDateTime.now().minusHours(3), null);

        AdminAccount superAdmin = adminAccountRepository.findByUsername(superUsername).orElseThrow();
        String token = login(superUsername, superPassword);

        mockMvc.perform(patch("/api/super-admin/work-requests/{id}/responsible-admin", request.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("adminId", superAdmin.getId()))))
                .andExpect(status().isBadRequest());
    }

    private String login(String username, String password) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        var response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/login")
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
        company.setAddress("Rua Principal " + nif);
        company.setCountry("Portugal");
        company.setStatus(false);
        return companyAccountRepository.save(company);
    }

    private TeamRequest createTeamRequest(CompanyAccount company, String name, State state,
                                          LocalDateTime createdAt, Integer responsibleAdminId) {
        TeamRequest request = new TeamRequest();
        request.setCompany(company);
        request.setTeamName(name);
        request.setState(state);
        request.setCreatedAt(createdAt);
        request.setResponsibleAdminId(responsibleAdminId);
        return teamRequestRepository.save(request);
    }

    private EmployeeRequest createEmployeeRequest(TeamRequest request, String role) {
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setTeamRequest(request);
        employeeRequest.setRequestedRole(role);
        return employeeRequestRepository.save(employeeRequest);
    }
}

