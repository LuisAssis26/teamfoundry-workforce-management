package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminWorkOfferServiceTest {

    @Mock TeamRequestRepository teamRequestRepository;
    @Mock EmployeeRequestRepository employeeRequestRepository;
    @Mock EmployeeRequestOfferRepository inviteRepository;
    @Mock EmployeeAccountRepository employeeAccountRepository;
    @Mock AdminAccountRepository adminAccountRepository;
    @Mock com.teamfoundry.backend.notification.service.NotificationService notificationService;

    @InjectMocks AdminWorkOfferService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendInvitesCreatesInvitesForOpenRequestsAndSkipsAcceptedAndDuplicates() {
        AdminAccount admin = new AdminAccount(5, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        TeamRequest team = new TeamRequest();
        team.setId(10);
        team.setResponsibleAdminId(5);
        team.setState(State.INCOMPLETE);
        when(teamRequestRepository.findById(10)).thenReturn(Optional.of(team));

        when(employeeRequestRepository.findAcceptedEmployeeIdsByTeam(10)).thenReturn(List.of(1));

        EmployeeRequest r1 = openRequest(100, team, "developer");
        EmployeeRequest r2 = openRequest(101, team, "developer");
        when(employeeRequestRepository.findByTeamRequest_IdAndRequestedRoleIgnoreCaseAndEmployeeIsNull(10, "developer"))
                .thenReturn(List.of(r1, r2));

        EmployeeAccount candidate = employee(2);
        when(employeeAccountRepository.findById(2)).thenReturn(Optional.of(candidate));
        when(employeeAccountRepository.findById(999)).thenReturn(Optional.empty());

        when(inviteRepository.existsByEmployeeRequest_IdAndEmployee_IdAndActiveTrue(100, 2)).thenReturn(false);
        when(inviteRepository.existsByEmployeeRequest_IdAndEmployee_IdAndActiveTrue(101, 2)).thenReturn(true);

        int created = service.sendInvites(10, "  Developer  ", List.of(1, 2, 999));

        assertThat(created).isEqualTo(1);

        ArgumentCaptor<List<EmployeeRequestOffer>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(inviteRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getEmployeeRequest().getId()).isEqualTo(100);
        assertThat(captor.getValue().get(0).getEmployee().getId()).isEqualTo(2);
        assertThat(captor.getValue().get(0).isActive()).isTrue();
    }

    @Test
    void sendInvitesWhenNotAuthenticatedThrowsUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.sendInvites(10, "dev", List.of(1)));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void sendInvitesWhenRequestNotAssignedThrowsForbidden() {
        AdminAccount admin = new AdminAccount(5, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        TeamRequest team = new TeamRequest();
        team.setId(10);
        team.setResponsibleAdminId(999);
        team.setState(State.INCOMPLETE);
        when(teamRequestRepository.findById(10)).thenReturn(Optional.of(team));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.sendInvites(10, "dev", List.of(1)));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sendInvitesWhenTeamIsCompleteThrowsConflict() {
        AdminAccount admin = new AdminAccount(5, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        TeamRequest team = new TeamRequest();
        team.setId(10);
        team.setResponsibleAdminId(5);
        team.setState(State.COMPLETED);
        when(teamRequestRepository.findById(10)).thenReturn(Optional.of(team));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.sendInvites(10, "dev", List.of(1)));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void sendInvitesWhenNoOpenRequestsForRoleThrowsConflict() {
        AdminAccount admin = new AdminAccount(5, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        TeamRequest team = new TeamRequest();
        team.setId(10);
        team.setResponsibleAdminId(5);
        team.setState(State.INCOMPLETE);
        when(teamRequestRepository.findById(10)).thenReturn(Optional.of(team));
        when(employeeRequestRepository.findAcceptedEmployeeIdsByTeam(10)).thenReturn(List.of());

        when(employeeRequestRepository.findByTeamRequest_IdAndRequestedRoleIgnoreCaseAndEmployeeIsNull(10, "developer"))
                .thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.sendInvites(10, "developer", List.of(2)));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void listActiveInviteIdsRequiresRoleAndAssignment() {
        AdminAccount admin = new AdminAccount(5, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        TeamRequest team = new TeamRequest();
        team.setId(10);
        team.setResponsibleAdminId(5);
        team.setState(State.INCOMPLETE);
        when(teamRequestRepository.findById(10)).thenReturn(Optional.of(team));

        when(inviteRepository.findActiveInviteEmployeeIdsByTeamAndRole(10, "developer")).thenReturn(List.of(1, 2));

        assertThat(service.listActiveInviteIds(10, "Developer")).containsExactly(1, 2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listActiveInviteIds(10, "  "));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listAcceptedIdsReturnsRepositoryResults() {
        AdminAccount admin = new AdminAccount(5, "admin", "pwd", UserType.ADMIN, false);
        authenticate(admin);
        when(adminAccountRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        TeamRequest team = new TeamRequest();
        team.setId(10);
        team.setResponsibleAdminId(5);
        team.setState(State.INCOMPLETE);
        when(teamRequestRepository.findById(10)).thenReturn(Optional.of(team));
        when(employeeRequestRepository.findAcceptedEmployeeIdsByTeam(10)).thenReturn(List.of(7, 9));

        assertThat(service.listAcceptedIds(10)).containsExactly(7, 9);
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

    private EmployeeRequest openRequest(int id, TeamRequest team, String role) {
        EmployeeRequest er = new EmployeeRequest();
        er.setId(id);
        er.setTeamRequest(team);
        er.setRequestedRole(role);
        er.setEmployee(null);
        return er;
    }

    private EmployeeAccount employee(int id) {
        EmployeeAccount e = new EmployeeAccount();
        e.setId(id);
        return e;
    }
}

