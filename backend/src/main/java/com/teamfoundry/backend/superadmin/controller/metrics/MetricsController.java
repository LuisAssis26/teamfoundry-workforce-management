package com.teamfoundry.backend.superadmin.controller.metrics;

import com.teamfoundry.backend.superadmin.dto.metrics.MetricsOverviewResponse;
import com.teamfoundry.backend.superadmin.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de métricas agregadas para o painel do super administrador.
 */
@RestController
@RequestMapping("/api/super-admin/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/overview")
    public MetricsOverviewResponse getOverview() {
        return metricsService.getOverview();
    }
}
