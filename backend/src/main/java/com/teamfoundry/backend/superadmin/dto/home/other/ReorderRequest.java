package com.teamfoundry.backend.superadmin.dto.home.other;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderRequest(
        @NotEmpty
        List<Long> ids
) {}
