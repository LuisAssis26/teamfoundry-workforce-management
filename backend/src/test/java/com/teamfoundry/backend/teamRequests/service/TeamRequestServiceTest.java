package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.common.service.ActionLogService;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.dto.AssignedAdminTeamRequestCount;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.TeamRequestResponse;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestOfferRepository;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TeamRequestServiceTest {

    @Mock TeamRequestRepository teamRequestRepository;
    @Mock AdminAccountRepository adminAccountRepository;
    @Mock EmployeeRequestRepository employeeRequestRepository;
    @Mock EmployeeRequestOfferRepository employeeRequestOfferRepository;
    @Mock ActionLogService actionLogService;

    @InjectMocks TeamRequestService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listAllWorkRequests_returnsSortedWithWorkforce() {
        TeamRequest first = buildRequest(1, "Alpha", LocalDateTime.now().minusDays(1));
        TeamRequest second = buildRequest(2, "Beta", LocalDateTime.now());

        when(teamRequestRepository.findAll(any(Sort.class))).thenReturn(List.of(second, first));
        when(employeeRequestRepository.countByTeamRequestIds(anyCollection())).thenReturn(List.of(
                countForRequest(second.getId(), 3),
                countForRequest(first.getId(), 1)
        ));

        List<TeamRequestResponse> responses = service.listAllWorkRequests();

        assertThat(responses).extracting(TeamRequestResponse::id).containsExactly(2, 1);
        assertThat(responses).extracting(TeamRequestResponse::workforceNeeded).containsExactly(3L, 1L);
    }

    @Test
    void listAssignableAdmins_filtersOnlyAdminsAndOrders() {
        AdminAccount adminA = new AdminAccount(1, "alpha", "pwd", UserType.ADMIN, false);
        AdminAccount adminB = new AdminAccount(2, "beta", "pwd", UserType.ADMIN, false);
        AdminAccount superAdmin = new AdminAccount(3, "super", "pwd", UserType.SUPERADMIN, false);

        when(adminAccountRepository.findByDeactivatedFalse(any(Sort.class)))
                .thenReturn(List.of(adminA, adminB, superAdmin));

        when(teamRequestRepository.countAssignmentsGroupedByAdmin()).thenReturn(List.of(
                assignment(adminA.getId(), 1),
                assignment(adminB.getId(), 2)
        ));

        List<AssignedAdminTeamRequestCount> admins = service.listAssignableAdmins();

        assertThat(admins).extracting(AssignedAdminTeamRequestCount::id).containsExactly(1, 2);
        assertThat(admins).extracting(AssignedAdminTeamRequestCount::requestCount).containsExactly(1L, 2L);
    }

    @Test
    void assignResponsibleAdmin_setsAdminAndLogs() {
        AdminAccount superAdmin = new AdminAccount(10, "superadmin", "pwd", UserType.SUPERADMIN, false);
        AdminAccount target = new AdminAccount(20, "admin", "pwd", UserType.ADMIN, false);
        authenticate(superAdmin);

        TeamRequest request = buildRequest(5, "Needs owner", LocalDateTime.now().minusHours(2));

        when(teamRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(adminAccountRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(adminAccountRepository.findByUsernameIgnoreCase(superAdmin.getUsername()))
                .thenReturn(Optional.of(superAdmin));
        when(teamRequestRepository.save(any(TeamRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRequestRepository.countByTeamRequestIds(anyCollection()))
                .thenReturn(new ArrayList<>());

        var response = service.assignResponsibleAdmin(request.getId(), target.getId());

        assertThat(response.responsibleAdminId()).isEqualTo(target.getId());
        verify(actionLogService).logAdmin(eq(superAdmin), any());
        assertThat(request.getResponsibleAdminId()).isEqualTo(target.getId());
    }

    @Test
    void assignResponsibleAdmin_withNonAdmin_throwsBadRequest() {
        AdminAccount superAdmin = new AdminAccount(10, "superadmin", "pwd", UserType.SUPERADMIN, false);
        AdminAccount notAdmin = new AdminAccount(30, "other", "pwd", UserType.SUPERADMIN, false);
        authenticate(superAdmin);

        TeamRequest request = buildRequest(7, "Invalid", LocalDateTime.now());

        when(teamRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(adminAccountRepository.findById(notAdmin.getId())).thenReturn(Optional.of(notAdmin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.assignResponsibleAdmin(request.getId(), notAdmin.getId()));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(teamRequestRepository, never()).save(any());
    }

    @Test
    void assignResponsibleAdmin_whenRequestMissing_throwsNotFound() {
        AdminAccount superAdmin = new AdminAccount(10, "superadmin", "pwd", UserType.SUPERADMIN, false);
        authenticate(superAdmin);

        when(teamRequestRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.assignResponsibleAdmin(999, 1));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void authenticate(AdminAccount admin) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin:" + admin.getUsername(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private TeamRequest buildRequest(int id, String teamName, LocalDateTime createdAt) {
        TeamRequest request = new TeamRequest();
        request.setId(id);
        request.setTeamName(teamName);
        request.setCreatedAt(createdAt);
        request.setState(State.INCOMPLETE);
        return request;
    }

    private TeamRequestRepository.AdminAssignmentCount assignment(Integer adminId, long total) {
        return new TeamRequestRepository.AdminAssignmentCount() {
            @Override
            public Integer getAdminId() {
                return adminId;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }

    private EmployeeRequestRepository.TeamRequestCount countForRequest(Integer requestId, long total) {
        return new EmployeeRequestRepository.TeamRequestCount() {
            @Override
            public Integer getRequestId() {
                return requestId;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }
}
