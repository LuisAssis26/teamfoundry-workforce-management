package com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HomeLoginSectionUpdateRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 500) String subtitle,
        @Size(max = 2000) String content,
        @Size(max = 80) String primaryCtaLabel,
        @Size(max = 300) String primaryCtaUrl,
        Boolean active,
        Boolean apiEnabled,
        @Size(max = 500) String apiUrl,
        @Size(max = 500) String apiToken,
        @Min(1) @Max(6) Integer apiMaxItems
) {
}
