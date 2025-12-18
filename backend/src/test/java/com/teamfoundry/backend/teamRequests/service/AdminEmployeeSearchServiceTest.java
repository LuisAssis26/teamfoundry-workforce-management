package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill;
import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import com.teamfoundry.backend.account.model.preferences.PrefRole;
import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.dto.search.AdminEmployeeSearchResponse;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminEmployeeSearchServiceTest {

    @Mock AdminAccountRepository adminAccountRepository;
    @Mock EmployeeAccountRepository employeeAccountRepository;
    @Mock EmployeeRoleRepository employeeRoleRepository;
    @Mock EmployeeSkillRepository employeeSkillRepository;
    @Mock EmployeeGeoAreaRepository employeeGeoAreaRepository;
    @Mock EmployeeRequestRepository employeeRequestRepository;

    @InjectMocks AdminEmployeeSearchService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchRequiresAuthenticatedAdmin() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.search("dev", List.of(), List.of()));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void searchNormalizesFiltersAndBuildsResponseWithLimitedExperiences() {
        AdminAccount admin = new AdminAccount(1, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        EmployeeAccount employee = new EmployeeAccount();
        employee.setId(10);
        employee.setName("Ana");
        employee.setSurname("Silva");
        employee.setEmail("ANA@EXAMPLE.COM");
        employee.setPhone("999");

        when(employeeAccountRepository.searchCandidates(any(), anyList(), anyBoolean(), anyList(), anyBoolean()))
                .thenReturn(List.of(employee));

        when(employeeRoleRepository.findFirstByEmployee(employee))
                .thenReturn(Optional.of(new com.teamfoundry.backend.account.model.employee.profile.EmployeeRole(
                        1, employee, new PrefRole(1, "Developer")
                )));

        when(employeeSkillRepository.findByEmployee(employee)).thenReturn(List.of(
                new EmployeeSkill(1, employee, new PrefSkill(1, "Java")),
                new EmployeeSkill(2, employee, new PrefSkill(2, ""))
        ));
        when(employeeGeoAreaRepository.findByEmployee(employee)).thenReturn(List.of(
                new EmployeeGeoArea(1, employee, new PrefGeoArea(1, "Lisboa")),
                new EmployeeGeoArea(2, employee, new PrefGeoArea(2, ""))
        ));

        List<EmployeeRequest> history = List.of(
                accepted("Dev", LocalDateTime.now().minusDays(2), concludedTeam(State.COMPLETE)),
                accepted("QA", LocalDateTime.now().minusDays(3), concludedTeamWithEndDate(LocalDateTime.now().minusDays(1))),
                accepted("PM", null, concludedTeam(State.COMPLETE)),
                accepted("UX", LocalDateTime.now().minusDays(4), openTeam())
        );
        when(employeeRequestRepository.findByEmployee_EmailOrderByAcceptedDateDesc("ana@example.com"))
                .thenReturn(history);

        List<AdminEmployeeSearchResponse> results = service.search(
                "  DEVELOPER ",
                List.of("  Lisboa ", "Lisboa", "  "),
                List.of(" JAVA ", "java")
        );

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> areasCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<Boolean> areasEmptyCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<List<String>> skillsCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<Boolean> skillsEmptyCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(employeeAccountRepository).searchCandidates(
                roleCaptor.capture(),
                areasCaptor.capture(),
                areasEmptyCaptor.capture(),
                skillsCaptor.capture(),
                skillsEmptyCaptor.capture()
        );

        assertThat(roleCaptor.getValue()).isEqualTo("developer");
        assertThat(areasCaptor.getValue()).containsExactly("lisboa");
        assertThat(areasEmptyCaptor.getValue()).isFalse();
        assertThat(skillsCaptor.getValue()).containsExactly("java");
        assertThat(skillsEmptyCaptor.getValue()).isFalse();

        assertThat(results).hasSize(1);
        AdminEmployeeSearchResponse response = results.get(0);
        assertThat(response.id()).isEqualTo(10);
        assertThat(response.email()).isEqualTo("ANA@EXAMPLE.COM");
        assertThat(response.role()).isEqualTo("Developer");
        assertThat(response.skills()).containsExactly("Java");
        assertThat(response.areas()).containsExactly("Lisboa");
        assertThat(response.experiences()).hasSize(2);
    }

    private void authenticate(AdminAccount admin) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin:" + admin.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private EmployeeRequest accepted(String requestedRole, LocalDateTime acceptedDate, TeamRequest team) {
        EmployeeRequest r = new EmployeeRequest();
        r.setRequestedRole(requestedRole);
        r.setAcceptedDate(acceptedDate);
        r.setTeamRequest(team);
        return r;
    }

    private TeamRequest concludedTeam(State state) {
        TeamRequest tr = new TeamRequest();
        tr.setState(state);
        tr.setEndDate(null);
        return tr;
    }

    private TeamRequest concludedTeamWithEndDate(LocalDateTime endDate) {
        TeamRequest tr = new TeamRequest();
        tr.setState(State.INCOMPLETE);
        tr.setEndDate(endDate);
        return tr;
    }

    private TeamRequest openTeam() {
        TeamRequest tr = new TeamRequest();
        tr.setState(State.INCOMPLETE);
        tr.setEndDate(LocalDateTime.now().plusDays(10));
        return tr;
    }
}


