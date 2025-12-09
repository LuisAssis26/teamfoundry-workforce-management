package com.teamfoundry.backend.account.dto.employee.preferences;

import java.util.List;

/**
 * DTO que expõe as opções de funções, competências e áreas geográficas para o front.
 */
public record EmployeePreferencesListResponse(
        List<String> functions,
        List<String> competences,
        List<String> geoAreas
) {
}
