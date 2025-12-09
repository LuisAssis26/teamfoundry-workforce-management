package com.teamfoundry.backend.teamRequests.dto.teamRequest;

import com.teamfoundry.backend.teamRequests.enums.State;
import java.time.LocalDateTime;

public record TeamRequestResponse(
        int id,
        String companyName,
        String companyEmail,
        String teamName,
        String description,
        State state,
        Integer responsibleAdminId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime createdAt
) {}
