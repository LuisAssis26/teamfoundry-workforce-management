package com.teamfoundry.backend.superadmin.dto.other;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record WeeklyTipRequest(
        @NotBlank @Size(max = 80)
        String category,

        @NotBlank @Size(max = 160)
        String title,

        @NotBlank @Size(max = 2000)
        String description,

        @NotNull
        LocalDate publishedAt,

        Boolean featured,

        Boolean active
) {
}

