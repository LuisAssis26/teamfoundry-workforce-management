package com.teamfoundry.backend.superadmin.dto.home.sections.noLogin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HomeNoLoginSectionUpdateRequest(
        @NotBlank @Size(max = 120)
        String title,

        @Size(max = 500)
        String subtitle,

        @Size(max = 80)
        String primaryCtaLabel,

        @Size(max = 300)
        String primaryCtaUrl,

        @Size(max = 80)
        String secondaryCtaLabel,

        @Size(max = 300)
        String secondaryCtaUrl,

        @NotNull
        Boolean active
) {}
