package com.teamfoundry.backend.superadmin.service.metrics;

import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.superadmin.dto.metrics.MetricsOverviewResponse;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço que reúne contagens simples para o painel de métricas do super admin.
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final CompanyAccountRepository companyAccountRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final TeamRequestRepository teamRequestRepository;
    private final AdminAccountRepository adminAccountRepository;

    public MetricsOverviewResponse getOverview() {
        long activeCompanies = companyAccountRepository.countByDeactivatedFalse();
        long pendingCompanies = companyAccountRepository.countByStatusFalseAndDeactivatedFalse();
        long activeEmployees = employeeAccountRepository.countByDeactivatedFalse();

        long openRequests = teamRequestRepository.countByState(State.INCOMPLETE);
        long closedRequests = teamRequestRepository.countByState(State.COMPLETED);

        List<MetricsOverviewResponse.StateCount> requestsByState = Arrays.stream(State.values())
                .map(state -> new MetricsOverviewResponse.StateCount(state.name(), teamRequestRepository.countByState(state)))
                .toList();

        Map<Integer, String> adminNames = adminAccountRepository.findAll().stream()
                .collect(Collectors.toMap(AdminAccount::getId, AdminAccount::getUsername));

        List<MetricsOverviewResponse.AdminWorkload> workloads = teamRequestRepository
                .countAssignmentsByState(State.INCOMPLETE)
                .stream()
                .map(row -> new MetricsOverviewResponse.AdminWorkload(
                        row.getAdminId(),
                        adminNames.getOrDefault(row.getAdminId(), "N/D"),
                        row.getTotal()
                ))
                .toList();

        MetricsOverviewResponse.Kpi kpis = new MetricsOverviewResponse.Kpi(
                activeCompanies,
                activeEmployees,
                pendingCompanies,
                openRequests,
                closedRequests
        );

        return new MetricsOverviewResponse(kpis, requestsByState, workloads);
    }
}
