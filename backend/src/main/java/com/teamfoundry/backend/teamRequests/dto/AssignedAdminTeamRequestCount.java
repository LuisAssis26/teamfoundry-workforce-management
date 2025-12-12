package com.teamfoundry.backend.teamRequests.dto;

public record AssignedAdminTeamRequestCount(
        int id,
        String username,
        long requestCount
) {}
