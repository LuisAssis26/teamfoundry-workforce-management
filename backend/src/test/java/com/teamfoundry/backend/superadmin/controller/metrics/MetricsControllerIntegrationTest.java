package com.teamfoundry.backend.superadmin.controller.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("MetricsController integration")
@Transactional
class MetricsControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminAccountRepository adminAccountRepository;
    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired EmployeeAccountRepository employeeAccountRepository;
    @Autowired TeamRequestRepository teamRequestRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String superUsername = "superadmin";
    private final String superPassword = "Super#Metrics123";

    @BeforeEach
    void setup() {
        teamRequestRepository.deleteAll();
        employeeAccountRepository.deleteAll();
        companyAccountRepository.deleteAll();
        adminAccountRepository.deleteAll();

        adminAccountRepository.save(new AdminAccount(0, superUsername,
                passwordEncoder.encode(superPassword), UserType.SUPERADMIN, false));
    }

    @Test
    @DisplayName("GET /metrics/overview agrega kpis, estados e workload")
    void overviewReturnsAggregatedMetrics() throws Exception {
        AdminAccount adminOne = adminAccountRepository.save(new AdminAccount(0, "admin-one",
                passwordEncoder.encode("Admin#1"), UserType.ADMIN, false));
        AdminAccount adminTwo = adminAccountRepository.save(new AdminAccount(0, "admin-two",
                passwordEncoder.encode("Admin#2"), UserType.ADMIN, false));

        CompanyAccount activeCompany = createCompany("active@comp.com", true, false);
        CompanyAccount pendingCompany = createCompany("pending@comp.com", false, false);
        createCompany("deactivated@comp.com", true, true);

        createEmployee("emp@user.com", false);
        createEmployee("emp2@user.com", true);

        createTeamRequest(activeCompany, State.INCOMPLETE, adminOne.getId());
        createTeamRequest(activeCompany, State.COMPLETE, adminOne.getId());
        createTeamRequest(pendingCompany, State.INCOMPLETE, adminTwo.getId());

        String token = login(superUsername, superPassword);

        mockMvc.perform(get("/api/super-admin/metrics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.activeCompanies").value(2))
                .andExpect(jsonPath("$.kpis.pendingCompanies").value(1))
                .andExpect(jsonPath("$.kpis.activeEmployees").value(1))
                .andExpect(jsonPath("$.kpis.openRequests").value(2))
                .andExpect(jsonPath("$.kpis.closedRequests").value(1))
                .andExpect(jsonPath("$.requestsByState", hasSize(2)))
                .andExpect(jsonPath("$.requestsByState[0].state").value("COMPLETE"))
                .andExpect(jsonPath("$.requestsByState[0].count").value(1))
                .andExpect(jsonPath("$.requestsByState[1].state").value("INCOMPLETE"))
                .andExpect(jsonPath("$.requestsByState[1].count").value(2))
                .andExpect(jsonPath("$.workloadByAdmin", hasSize(2)))
                .andExpect(jsonPath("$.workloadByAdmin[*].adminName", containsInAnyOrder("admin-one", "admin-two")))
                .andExpect(jsonPath("$.workloadByAdmin[*].pendingRequests", containsInAnyOrder(1, 1)));
    }

    private CompanyAccount createCompany(String email, boolean status, boolean deactivated) {
        CompanyAccount company = new CompanyAccount();
        company.setEmail(email);
        company.setPassword(passwordEncoder.encode("Company#123"));
        company.setRole(UserType.COMPANY);
        company.setName("Company " + email);
        company.setAddress("Rua "+email.hashCode());
        company.setCountry("Portugal");
        company.setStatus(status);
        company.setDeactivated(deactivated);
        company.setRegistrationStatus(RegistrationStatus.COMPLETED);
        return companyAccountRepository.save(company);
    }

    private EmployeeAccount createEmployee(String email, boolean deactivated) {
        EmployeeAccount employee = new EmployeeAccount();
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode("Emp#123"));
        employee.setRole(UserType.EMPLOYEE);
        employee.setVerified(true);
        employee.setDeactivated(deactivated);
        employee.setRegistrationStatus(RegistrationStatus.COMPLETED);
        return employeeAccountRepository.save(employee);
    }

    private TeamRequest createTeamRequest(CompanyAccount company, State state, Integer adminId) {
        TeamRequest request = new TeamRequest();
        request.setCompany(company);
        request.setTeamName("Team " + state + " " + adminId);
        request.setState(state);
        request.setResponsibleAdminId(adminId);
        request.setCreatedAt(LocalDateTime.now());
        return teamRequestRepository.save(request);
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
}

