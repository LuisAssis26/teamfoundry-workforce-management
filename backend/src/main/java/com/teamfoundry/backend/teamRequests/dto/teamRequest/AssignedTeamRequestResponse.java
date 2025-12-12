package com.teamfoundry.backend.teamRequests.dto.teamRequest;

import com.teamfoundry.backend.teamRequests.enums.State;
import java.time.LocalDateTime;

public record AssignedTeamRequestResponse(
        int id,
        String companyName,
        String companyEmail,
        String companyPhone,
        long workforceNeeded,
        State state,
        LocalDateTime createdAt
) {}
