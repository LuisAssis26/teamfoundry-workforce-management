package com.teamfoundry.backend.superadmin.dto.home.other;

import jakarta.validation.constraints.NotNull;

public record ToggleVisibilityRequest(
        @NotNull
        Boolean active
) {}
