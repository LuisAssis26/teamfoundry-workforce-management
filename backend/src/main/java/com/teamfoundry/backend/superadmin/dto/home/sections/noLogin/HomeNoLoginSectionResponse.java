package com.teamfoundry.backend.superadmin.dto.home.sections.noLogin;

import com.teamfoundry.backend.superadmin.enums.SiteSectionType;

public record HomeNoLoginSectionResponse(
        Long id,
        SiteSectionType type,
        boolean active,
        int displayOrder,
        String title,
        String subtitle,
        String primaryCtaLabel,
        String primaryCtaUrl,
        String secondaryCtaLabel,
        String secondaryCtaUrl
) {}
