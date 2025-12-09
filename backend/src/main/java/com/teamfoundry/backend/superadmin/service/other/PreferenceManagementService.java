package com.teamfoundry.backend.superadmin.service.other;

import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import com.teamfoundry.backend.account.model.preferences.PrefRole;
import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import com.teamfoundry.backend.account.repository.preferences.PrefActivitySectorsRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefSkillRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefGeoAreaRepository;
import com.teamfoundry.backend.superadmin.dto.other.CreatePreferenceRequest;
import com.teamfoundry.backend.superadmin.dto.other.CreatePreferenceResponse;
import com.teamfoundry.backend.superadmin.repository.other.CreatePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio para criar, listar e remover opcoes globais
 * (funcoes, competencias, areas geograficas e setores de atividade).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PreferenceManagementService {

    private final CreatePreferenceRepository functionRepository;
    private final PrefSkillRepository prefSkillRepository;
    private final PrefGeoAreaRepository prefGeoAreaRepository;
    private final PrefActivitySectorsRepository prefActivitySectorsRepository;

    // -------- Funcoes --------

    /**
     * Lista todas as funcoes.
     */
    @Transactional(readOnly = true)
    public List<CreatePreferenceResponse> listFunctions() {
        return functionRepository.findAll().stream()
                .map(entity -> new CreatePreferenceResponse((long) entity.getId(), entity.getName()))
                .toList();
    }

    /**
     * Cria uma nova funcao.
     */
    public CreatePreferenceResponse createFunction(CreatePreferenceRequest request) {
        String normalized = normalize(request);
        ensureNotExists(normalized, name -> functionRepository.findByNameIgnoreCase(name));
        PrefRole entity = new PrefRole();
        entity.setName(normalized);
        PrefRole saved = functionRepository.save(entity);
        return new CreatePreferenceResponse((long) saved.getId(), saved.getName());
    }

    /**
     * Apaga uma funcao existente.
     */
    public void deleteFunction(Integer id) {
        PrefRole function = functionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcao nao encontrada."));
        functionRepository.delete(function);
    }

    // -------- Competencias --------

    @Transactional(readOnly = true)
    public List<CreatePreferenceResponse> listCompetences() {
        return prefSkillRepository.findAll().stream()
                .map(entity -> new CreatePreferenceResponse((long) entity.getId(), entity.getName()))
                .toList();
    }

    public CreatePreferenceResponse createCompetence(CreatePreferenceRequest request) {
        String normalized = normalize(request);
        ensureNotExists(normalized, name -> prefSkillRepository.findByNameIgnoreCase(name));
        PrefSkill entity = new PrefSkill();
        entity.setName(normalized);
        PrefSkill saved = prefSkillRepository.save(entity);
        return new CreatePreferenceResponse((long) saved.getId(), saved.getName());
    }

    public void deleteCompetence(Integer id) {
        PrefSkill prefSkill = prefSkillRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competencia nao encontrada."));
        prefSkillRepository.delete(prefSkill);
    }

    // -------- Areas geograficas --------

    @Transactional(readOnly = true)
    public List<CreatePreferenceResponse> listGeoAreas() {
        return prefGeoAreaRepository.findAll().stream()
                .map(entity -> new CreatePreferenceResponse((long) entity.getId(), entity.getName()))
                .toList();
    }

    public CreatePreferenceResponse createGeoArea(CreatePreferenceRequest request) {
        String normalized = normalize(request);
        ensureNotExists(normalized, name -> prefGeoAreaRepository.findByNameIgnoreCase(name));
        PrefGeoArea entity = new PrefGeoArea();
        entity.setName(normalized);
        PrefGeoArea saved = prefGeoAreaRepository.save(entity);
        return new CreatePreferenceResponse((long) saved.getId(), saved.getName());
    }

    public void deleteGeoArea(Integer id) {
                    PrefGeoArea area = prefGeoAreaRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Area geografica nao encontrada."));
        prefGeoAreaRepository.delete(area);
    }

    // -------- Setores de atividade --------

    @Transactional(readOnly = true)
    public List<CreatePreferenceResponse> listActivitySectors() {
        return prefActivitySectorsRepository.findAll().stream()
                .map(entity -> new CreatePreferenceResponse((long) entity.getId(), entity.getName()))
                .toList();
    }

    public CreatePreferenceResponse createActivitySector(CreatePreferenceRequest request) {
        String normalized = normalize(request);
        ensureNotExists(normalized, name -> prefActivitySectorsRepository.findByNameIgnoreCase(name));
        PrefActivitySectors entity = new PrefActivitySectors();
        entity.setName(normalized);
        PrefActivitySectors saved = prefActivitySectorsRepository.save(entity);
        return new CreatePreferenceResponse((long) saved.getId(), saved.getName());
    }

    public void deleteActivitySector(Integer id) {
        PrefActivitySectors sector = prefActivitySectorsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setor de atividade nao encontrado."));
        prefActivitySectorsRepository.delete(sector);
    }

    // -------- Helpers --------

    private String normalize(CreatePreferenceRequest request) {
        String normalized = request != null && request.name() != null ? request.name().trim() : "";
        if (!StringUtils.hasText(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome e obrigatorio.");
        }
        return normalized;
    }

    private void ensureNotExists(String name, java.util.function.Function<String, Optional<?>> finder) {
        finder.apply(name).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um registo com esse nome.");
        });
    }
}
