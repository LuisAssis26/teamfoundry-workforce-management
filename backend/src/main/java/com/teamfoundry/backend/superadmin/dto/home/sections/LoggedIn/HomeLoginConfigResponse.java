package com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn;

import java.util.List;

public record HomeLoginConfigResponse(
        List<HomeLoginSectionResponse> sections,
        List<HomeLoginMetricResponse> metrics
) {
}
