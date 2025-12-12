package com.teamfoundry.backend.account.controller;

import com.teamfoundry.backend.account.dto.employee.preferences.EmployeePreferencesListResponse;
import com.teamfoundry.backend.account.service.ProfilePreferencesListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposição REST das opções utilizadas pelo registo de candidato (funções, competências e áreas).
 */
@RestController
@RequestMapping("/api/profile-options")
@RequiredArgsConstructor
public class PreferencesListController {

    private final ProfilePreferencesListService profilePreferencesListService;

    /**
     * Obtém todas as opções pré-carregadas para o fluxo de preferências do candidato.
     *
     * @return objeto JSON com listas de funções, competências e áreas geográficas.
     */
    @GetMapping
    public ResponseEntity<EmployeePreferencesListResponse> listOptions() {
        return ResponseEntity.ok(profilePreferencesListService.fetchOptions());
    }
}
