package com.teamfoundry.backend.teamRequests.controller;

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
import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequestOffer;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestOfferRepository;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AdminTeamRequestController integration")
@Transactional
class AdminTeamRequestControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired AdminAccountRepository adminAccountRepository;
    @Autowired TeamRequestRepository teamRequestRepository;
    @Autowired EmployeeRequestRepository employeeRequestRepository;
    @Autowired EmployeeRequestOfferRepository employeeRequestOfferRepository;
    @Autowired CompanyAccountRepository companyAccountRepository;
    @Autowired EmployeeAccountRepository employeeAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String adminUsername = "admin-default";
    private final String adminPassword = "Admin#123";
    private int adminId;

    @BeforeEach
    void setup() {
        employeeRequestOfferRepository.deleteAll();
        employeeRequestRepository.deleteAll();
        teamRequestRepository.deleteAll();
        companyAccountRepository.deleteAll();
        employeeAccountRepository.deleteAll();
        adminAccountRepository.deleteAll();

        AdminAccount saved = adminAccountRepository.save(new AdminAccount(0, adminUsername,
                passwordEncoder.encode(adminPassword), UserType.ADMIN, false));
        adminId = saved.getId();
    }

    @Test
    void listAssignedRequestsReturnsWorkforceCounts() throws Exception {
        String token = login(adminUsername, adminPassword);
        CompanyAccount company = createCompany("company@test.com", 123456789);
        TeamRequest assigned = createTeamRequest(company, "Team", State.INCOMPLETE, LocalDateTime.now(), adminId);
        createEmployeeRequest(assigned, "developer", null);
        createEmployeeRequest(assigned, "qa", null);

        mockMvc.perform(get("/api/admin/work-requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(assigned.getId()))
                .andExpect(jsonPath("$[0].workforceNeeded").value(2));
    }

    @Test
    void getAssignedRequestReturnsDetails() throws Exception {
        String token = login(adminUsername, adminPassword);
        CompanyAccount company = createCompany("company2@test.com", 987654321);
        TeamRequest assigned = createTeamRequest(company, "Alpha", State.INCOMPLETE, LocalDateTime.now(), adminId);
        createEmployeeRequest(assigned, "developer", null);

        mockMvc.perform(get("/api/admin/work-requests/{id}", assigned.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(assigned.getId()))
                .andExpect(jsonPath("$.teamName").value("Alpha"))
                .andExpect(jsonPath("$.workforceNeeded").value(1));
    }

    @Test
    void getAssignedRequestWhenNotAssignedReturnsForbidden() throws Exception {
        String token = login(adminUsername, adminPassword);
        CompanyAccount company = createCompany("other@test.com", 111222333);
        TeamRequest other = createTeamRequest(company, "Other", State.INCOMPLETE, LocalDateTime.now(), adminId + 999);

        mockMvc.perform(get("/api/admin/work-requests/{id}", other.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listRolesReturnsAggregatedCountsAndInvites() throws Exception {
        String token = login(adminUsername, adminPassword);
        CompanyAccount company = createCompany("roles@test.com", 444555666);
        TeamRequest assigned = createTeamRequest(company, "Roles", State.INCOMPLETE, LocalDateTime.now(), adminId);

        EmployeeAccount filledEmployee = employee("filled@test.com");
        EmployeeAccount invitedEmployee = employee("invited@test.com");
        employeeAccountRepository.saveAll(java.util.List.of(filledEmployee, invitedEmployee));

        createEmployeeRequest(assigned, "developer", null);
        EmployeeRequest openDev = createEmployeeRequest(assigned, "developer", null);
        createEmployeeRequest(assigned, "developer", filledEmployee);
        EmployeeRequest openQa = createEmployeeRequest(assigned, "qa", null);

        employeeRequestOfferRepository.save(invite(openDev, invitedEmployee));
        employeeRequestOfferRepository.save(invite(openQa, invitedEmployee));

        mockMvc.perform(get("/api/admin/work-requests/{id}/roles", assigned.getId())
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.role=='developer')].totalPositions").value(org.hamcrest.Matchers.contains(3)))
                .andExpect(jsonPath("$[?(@.role=='developer')].filledPositions").value(org.hamcrest.Matchers.contains(1)))
                .andExpect(jsonPath("$[?(@.role=='developer')].openPositions").value(org.hamcrest.Matchers.contains(2)))
                .andExpect(jsonPath("$[?(@.role=='developer')].proposalsSent").value(org.hamcrest.Matchers.contains(1)))
                .andExpect(jsonPath("$[?(@.role=='qa')].totalPositions").value(org.hamcrest.Matchers.contains(1)))
                .andExpect(jsonPath("$[?(@.role=='qa')].proposalsSent").value(org.hamcrest.Matchers.contains(1)));
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

    private EmployeeRequest createEmployeeRequest(TeamRequest request, String role, EmployeeAccount employee) {
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setTeamRequest(request);
        employeeRequest.setRequestedRole(role);
        employeeRequest.setEmployee(employee);
        if (employee != null) {
            employeeRequest.setAcceptedDate(LocalDateTime.now().minusDays(1));
        }
        return employeeRequestRepository.save(employeeRequest);
    }

    private EmployeeAccount employee(String email) {
        EmployeeAccount e = new EmployeeAccount();
        e.setEmail(email);
        e.setPassword(passwordEncoder.encode("empPass"));
        e.setRole(UserType.EMPLOYEE);
        e.setVerified(true);
        e.setDeactivated(false);
        e.setRegistrationStatus(RegistrationStatus.COMPLETED);
        e.setName("Emp");
        e.setSurname("Test");
        return e;
    }

    private EmployeeRequestOffer invite(EmployeeRequest request, EmployeeAccount employee) {
        EmployeeRequestOffer offer = new EmployeeRequestOffer();
        offer.setEmployeeRequest(request);
        offer.setEmployee(employee);
        offer.setActive(true);
        return offer;
    }
}

