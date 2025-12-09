package com.teamfoundry.backend.account.dto.employee.documents;

import lombok.Builder;
import lombok.Value;

/**
 * Resumo compacto do perfil do colaborador para o dashboard/home autenticada.
 */
@Value
@Builder
public class EmployeeProfileSummaryResponse {
    int profileCompletionPercentage;
    boolean hasCurriculum;
    boolean hasProfilePicture;
    boolean hasRolePreference;
    boolean hasCompetences;
    boolean hasGeoAreas;
    long openOffers;
    String currentCompanyName;
}
