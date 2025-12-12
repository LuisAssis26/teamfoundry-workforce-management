package com.teamfoundry.backend.superadmin.controller.website;

import com.teamfoundry.backend.superadmin.dto.other.CreatePreferenceRequest;
import com.teamfoundry.backend.superadmin.dto.other.CreatePreferenceResponse;
import com.teamfoundry.backend.superadmin.service.other.PreferenceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Exposição REST para criação de novas funções.
 */
@RestController
@RequestMapping("/api/functions")
@RequiredArgsConstructor
public class CreatePreferenceController {

    private final PreferenceManagementService service;

    /**
     * Cria uma nova função a partir do nome enviado no payload.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePreferenceResponse create(@Valid @RequestBody CreatePreferenceRequest request) {
        return service.createFunction(request);
    }

    /**
     * Lista todas as funções existentes.
     */
    @GetMapping
    public java.util.List<CreatePreferenceResponse> list() {
        return service.listFunctions();
    }

    /**
     * Remove uma função existente pelo seu ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.deleteFunction(id);
    }

    /**
     * Lista todas as competências.
     */
    @GetMapping("/competences")
    public java.util.List<CreatePreferenceResponse> listCompetences() {
        return service.listCompetences();
    }

    /**
     * Cria uma competência.
     */
    @PostMapping("/competences")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePreferenceResponse createCompetence(@Valid @RequestBody CreatePreferenceRequest request) {
        return service.createCompetence(request);
    }

    /**
     * Remove uma competência pelo ID.
     */
    @DeleteMapping("/competences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompetence(@PathVariable Integer id) {
        service.deleteCompetence(id);
    }

    /**
     * Lista todas as áreas geográficas.
     */
    @GetMapping("/geo-areas")
    public java.util.List<CreatePreferenceResponse> listGeoAreas() {
        return service.listGeoAreas();
    }

    /**
     * Cria uma área geográfica.
     */
    @PostMapping("/geo-areas")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePreferenceResponse createGeoArea(@Valid @RequestBody CreatePreferenceRequest request) {
        return service.createGeoArea(request);
    }

    /**
     * Remove uma área geográfica.
     */
    @DeleteMapping("/geo-areas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGeoArea(@PathVariable Integer id) {
        service.deleteGeoArea(id);
    }

    /**
     * Lista todos os setores de atividade.
     */
    @GetMapping("/activity-sectors")
    public java.util.List<CreatePreferenceResponse> listActivitySectors() {
        return service.listActivitySectors();
    }

    /**
     * Cria um setor de atividade.
     */
    @PostMapping("/activity-sectors")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePreferenceResponse createActivitySector(@Valid @RequestBody CreatePreferenceRequest request) {
        return service.createActivitySector(request);
    }

    /**
     * Remove um setor de atividade.
     */
    @DeleteMapping("/activity-sectors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivitySector(@PathVariable Integer id) {
        service.deleteActivitySector(id);
    }
}
