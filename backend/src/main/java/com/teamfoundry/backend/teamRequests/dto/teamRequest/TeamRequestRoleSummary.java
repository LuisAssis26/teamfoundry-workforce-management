package com.teamfoundry.backend.teamRequests.dto.teamRequest;

public record TeamRequestRoleSummary(
        String role,
        long totalPositions,
        long filledPositions,
        long openPositions,
        long proposalsSent
) {}
