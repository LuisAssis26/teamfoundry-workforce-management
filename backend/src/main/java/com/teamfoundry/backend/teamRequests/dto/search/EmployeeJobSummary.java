package com.teamfoundry.backend.teamRequests.dto.search;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Resumo das ofertas e histórico do colaborador.
 */
@Value
@Builder
public class EmployeeJobSummary {
    int requestId;
    String teamName;
    String companyName;
    String location;
    String description;
    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime acceptedDate;
    String requestedRole;
    String status;
}
