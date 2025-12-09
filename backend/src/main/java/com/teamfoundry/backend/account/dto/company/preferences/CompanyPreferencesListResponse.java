package com.teamfoundry.backend.account.dto.company.preferences;

import java.util.List;

public record CompanyPreferencesListResponse(
        List<String> activitySectors,
        List<String> countries
) {}
