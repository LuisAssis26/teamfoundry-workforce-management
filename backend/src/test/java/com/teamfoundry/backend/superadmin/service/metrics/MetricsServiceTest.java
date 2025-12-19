package com.teamfoundry.backend.superadmin.service.metrics;

import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.superadmin.dto.metrics.MetricsOverviewResponse;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MetricsServiceTest {

    @Mock CompanyAccountRepository companyAccountRepository;
    @Mock EmployeeAccountRepository employeeAccountRepository;
    @Mock TeamRequestRepository teamRequestRepository;
    @Mock AdminAccountRepository adminAccountRepository;

    @InjectMocks MetricsService service;

    @Test
    void getOverviewReturnsAggregatedCounts() {
        when(companyAccountRepository.countByDeactivatedFalse()).thenReturn(5L);
        when(companyAccountRepository.countByStatusFalseAndDeactivatedFalse()).thenReturn(2L);
        when(employeeAccountRepository.countByDeactivatedFalse()).thenReturn(7L);

        when(teamRequestRepository.countByState(State.COMPLETE)).thenReturn(3L);
        when(teamRequestRepository.countByState(State.INCOMPLETE)).thenReturn(4L);

        when(teamRequestRepository.countAssignmentsByState(State.INCOMPLETE)).thenReturn(List.of(
                assignment(10, 2),
                assignment(20, 5)
        ));

        AdminAccount adminA = new AdminAccount(10, "alpha", "pwd", com.teamfoundry.backend.account.enums.UserType.ADMIN, false);
        AdminAccount adminB = new AdminAccount(20, "beta", "pwd", com.teamfoundry.backend.account.enums.UserType.ADMIN, false);
        when(adminAccountRepository.findAll()).thenReturn(List.of(adminA, adminB));

        MetricsOverviewResponse response = service.getOverview();

        assertThat(response.kpis().activeCompanies()).isEqualTo(5);
        assertThat(response.kpis().pendingCompanies()).isEqualTo(2);
        assertThat(response.kpis().activeEmployees()).isEqualTo(7);
        assertThat(response.kpis().openRequests()).isEqualTo(4);
        assertThat(response.kpis().closedRequests()).isEqualTo(3);

        assertThat(response.requestsByState()).extracting(MetricsOverviewResponse.StateCount::state)
                .containsExactly("COMPLETE", "INCOMPLETE");
        assertThat(response.requestsByState()).extracting(MetricsOverviewResponse.StateCount::count)
                .containsExactly(3L, 4L);

        Map<Integer, Long> workloads = response.workloadByAdmin().stream()
                .collect(java.util.stream.Collectors.toMap(
                        MetricsOverviewResponse.AdminWorkload::adminId,
                        MetricsOverviewResponse.AdminWorkload::pendingRequests
                ));
        assertThat(workloads).containsEntry(10, 2L).containsEntry(20, 5L);
    }

    @Test
    void getOverviewWhenAdminNotFoundSetsNameAsDefault() {
        when(companyAccountRepository.countByDeactivatedFalse()).thenReturn(0L);
        when(companyAccountRepository.countByStatusFalseAndDeactivatedFalse()).thenReturn(0L);
        when(employeeAccountRepository.countByDeactivatedFalse()).thenReturn(0L);
        when(teamRequestRepository.countByState(any(State.class))).thenReturn(0L);

        when(teamRequestRepository.countAssignmentsByState(State.INCOMPLETE)).thenReturn(List.of(
                assignment(99, 1)
        ));

        when(adminAccountRepository.findAll()).thenReturn(List.of());

        MetricsOverviewResponse response = service.getOverview();

        assertThat(response.workloadByAdmin()).hasSize(1);
        MetricsOverviewResponse.AdminWorkload workload = response.workloadByAdmin().get(0);
        assertThat(workload.adminId()).isEqualTo(99);
        assertThat(workload.adminName()).isEqualTo("N/D");
        assertThat(workload.pendingRequests()).isEqualTo(1);
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
}

