package com.teamfoundry.backend.superadmin.dto.other;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePreferenceRequest(
        @NotBlank @Size(max = 120) String name
) {
}
