package com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn;

public record HomeLoginMetricResponse(
        Long id,
        String label,
        String value,
        String description,
        boolean active,
        int displayOrder
) {
}
