package com.teamfoundry.backend.superadmin.dto.home.showcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartnerShowcaseRequest(
        @NotBlank @Size(max = 150)
        String name,

        @NotBlank @Size(max = 800)
        String description,

        @NotBlank @Size(max = 500)
        String imageUrl,

        @Size(max = 500)
        String websiteUrl,

        @NotNull
        Boolean active
) {}
