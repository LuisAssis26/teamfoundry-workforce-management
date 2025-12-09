package com.teamfoundry.backend.account.service;

import com.teamfoundry.backend.account.dto.employee.preferences.EmployeePreferencesListResponse;
import com.teamfoundry.backend.account.repository.preferences.PrefSkillRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefRoleRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefGeoAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Serviço responsável por reunir as listas de opções utilizadas no passo de preferências.
 */
@Service
@RequiredArgsConstructor
public class ProfilePreferencesListService {

    private final PrefRoleRepository prefRoleRepository;
    private final PrefSkillRepository prefSkillRepository;
    private final PrefGeoAreaRepository prefGeoAreaRepository;

    /**
     * Devolve todas as opções disponíveis (ordenadas alfabeticamente) para o frontend.
     *
     * @return DTO contendo listas de funções, competências e áreas geográficas.
     */
    public EmployeePreferencesListResponse fetchOptions() {
        var functions = prefRoleRepository.findAll().stream()
                .map(pref -> pref.getName())
                .sorted()
                .collect(Collectors.toList());

        var competences = prefSkillRepository.findAll().stream()
                .map(skill -> skill.getName())
                .sorted()
                .collect(Collectors.toList());

        var geoAreas = prefGeoAreaRepository.findAll().stream()
                .map(area -> area.getName())
                .sorted()
                .collect(Collectors.toList());

        return new EmployeePreferencesListResponse(functions, competences, geoAreas);
    }
}
