package com.teamfoundry.backend.superadmin.dto.metrics;

import java.util.List;

/**
 * Resposta agregada com métricas principais para o painel de super admin.
 */
public record MetricsOverviewResponse(
        Kpi kpis,
        List<StateCount> requestsByState,
        List<AdminWorkload> workloadByAdmin
) {

    public record Kpi(
            long activeCompanies,
            long activeEmployees,
            long pendingCompanies,
            long openRequests,
            long closedRequests
    ) {
    }

    public record StateCount(
            String state,
            long count
    ) {
    }

    public record AdminWorkload(
            Integer adminId,
            String adminName,
            long pendingRequests
    ) {
    }
}
