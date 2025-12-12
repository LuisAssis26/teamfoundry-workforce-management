package com.teamfoundry.backend.superadmin.dto.teamRequests;

import jakarta.validation.constraints.NotNull;

public record AssignAdminRequest(
        @NotNull Integer adminId
) {}
