package com.teamfoundry.backend.superadmin.dto.home;

import java.util.List;

public record HomeUnifiedUpdateRequest(
        List<Long> publicSectionIds,
        List<Long> authenticatedSectionIds
) {
}
