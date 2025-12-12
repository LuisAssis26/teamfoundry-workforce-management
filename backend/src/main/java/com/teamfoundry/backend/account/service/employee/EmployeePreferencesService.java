package com.teamfoundry.backend.account.service.employee;

import com.teamfoundry.backend.account.dto.employee.preferences.EmployeePreferencesResponse;
import com.teamfoundry.backend.account.dto.employee.preferences.EmployeePreferencesUpdateRequest;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeRole;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea;
import com.teamfoundry.backend.account.model.preferences.PrefRole;
import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import com.teamfoundry.backend.account.repository.preferences.PrefSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefRoleRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefGeoAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployeePreferencesService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;
    private final PrefRoleRepository prefRoleRepository;
    private final PrefSkillRepository prefSkillRepository;
    private final PrefGeoAreaRepository prefGeoAreaRepository;

    @Transactional(readOnly = true)
    public EmployeePreferencesResponse getPreferences(String email) {
        EmployeeAccount account = findByEmailOrThrow(email);
        return toResponse(account);
    }

    @Transactional
    public EmployeePreferencesResponse updatePreferences(String email, EmployeePreferencesUpdateRequest request) {
        EmployeeAccount account = findByEmailOrThrow(email);
        applyFunctionPreference(account, request.getRole());
        applyCompetencePreferences(account, request.getSkills());
        applyGeoAreaPreferences(account, request.getAreas());
        return toResponse(account);
    }

    private EmployeePreferencesResponse toResponse(EmployeeAccount account) {
        // Constrói DTO a partir das relações atuais nas tabelas de junção.
        String role = employeeRoleRepository.findFirstByEmployee(account)
                .map(rel -> rel.getFunction().getName())
                .orElse(null);

        List<String> skills = employeeSkillRepository.findByEmployee(account).stream()
                .map(rel -> rel.getPrefSkill().getName())
                .toList();

        List<String> areas = employeeGeoAreaRepository.findByEmployee(account).stream()
                .map(rel -> rel.getGeoArea().getName())
                .toList();

        return EmployeePreferencesResponse.builder()
                .role(role)
                .skills(skills)
                .areas(areas)
                .build();
    }

    private EmployeeAccount findByEmailOrThrow(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador não autenticado.");
        }
        return employeeAccountRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
    }

    private void applyFunctionPreference(EmployeeAccount account, String functionName) {
        // Apenas uma função preferencial é mantida; limpamos e regravamos.
        employeeRoleRepository.deleteByEmployee(account);

        if (!StringUtils.hasText(functionName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Função preferencial é obrigatória.");
        }

        PrefRole function = prefRoleRepository.findByName(functionName.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Função não encontrada: " + functionName
                ));

        EmployeeRole relation = new EmployeeRole();
        relation.setEmployee(account);
        relation.setFunction(function);
        employeeRoleRepository.save(relation);
    }

    private void applyCompetencePreferences(EmployeeAccount account, List<String> skills) {
        // Zera competências atuais e recria as relações com base no request.
        employeeSkillRepository.deleteByEmployee(account);

        List<String> normalized = normalizeList(skills);
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione pelo menos uma competência.");
        }

        List<EmployeeSkill> relations = normalized.stream()
                .map(name -> {
                    PrefSkill prefSkill = prefSkillRepository.findByName(name)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Competência não encontrada: " + name
                            ));
                    EmployeeSkill relation = new EmployeeSkill();
                    relation.setEmployee(account);
                    relation.setPrefSkill(prefSkill);
                    return relation;
                })
                .toList();

        employeeSkillRepository.saveAll(relations);
    }

    private void applyGeoAreaPreferences(EmployeeAccount account, List<String> areas) {
        // Zera áreas atuais e recria relações com base no request.
        employeeGeoAreaRepository.deleteByEmployee(account);

        List<String> normalized = normalizeList(areas);
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione pelo menos uma área geográfica.");
        }

        List<EmployeeGeoArea> relations = normalized.stream()
                .map(name -> {
                    PrefGeoArea area = prefGeoAreaRepository.findByName(name)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Área geográfica não encontrada: " + name
                            ));
                    EmployeeGeoArea relation = new EmployeeGeoArea();
                    relation.setEmployee(account);
                    relation.setGeoArea(area);
                    return relation;
                })
                .toList();

        employeeGeoAreaRepository.saveAll(relations);
    }

    private List<String> normalizeList(List<String> values) {
        if (Objects.isNull(values)) {
            return List.of();
        }
        return values.stream()
                .map(value -> value == null ? null : value.trim())
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }
}
