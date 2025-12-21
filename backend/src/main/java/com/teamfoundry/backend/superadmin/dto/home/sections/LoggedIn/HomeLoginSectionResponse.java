package com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn;

import com.teamfoundry.backend.superadmin.enums.HomeLoginSectionType;

public record HomeLoginSectionResponse(
        Long id,
        HomeLoginSectionType type,
        boolean active,
        int displayOrder,
        String title,
        String subtitle,
        String content,
        String primaryCtaLabel,
        String primaryCtaUrl,
        String greetingPrefix,
        boolean profileBarVisible,
        String labelCurrentCompany,
        String labelOffers
) {
}
