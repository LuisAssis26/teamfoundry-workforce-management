package com.teamfoundry.backend.site.service;

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
import com.teamfoundry.backend.superadmin.service.other.PreferenceManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PreferenceManagementServiceTest {

    private CreatePreferenceRepository functionRepository;
    private PrefSkillRepository prefSkillRepository;
    private PrefGeoAreaRepository prefGeoAreaRepository;
    private PrefActivitySectorsRepository prefActivitySectorsRepository;
    private PreferenceManagementService service;

    @BeforeEach
    void setUp() {
        functionRepository = mock(CreatePreferenceRepository.class);
        prefSkillRepository = mock(PrefSkillRepository.class);
        prefGeoAreaRepository = mock(PrefGeoAreaRepository.class);
        prefActivitySectorsRepository = mock(PrefActivitySectorsRepository.class);
        service = new PreferenceManagementService(
                functionRepository,
                prefSkillRepository,
                prefGeoAreaRepository,
                prefActivitySectorsRepository
        );
    }

    // -------- Funcoes --------

    @Test
    void criaFuncaoComSucesso() {
        CreatePreferenceRequest request = new CreatePreferenceRequest("  Soldador  ");

        when(functionRepository.findByNameIgnoreCase("Soldador")).thenReturn(Optional.empty());
        when(functionRepository.save(any(PrefRole.class))).thenAnswer(invocation -> {
            PrefRole entity = invocation.getArgument(0);
            entity.setId(10);
            return entity;
        });

        CreatePreferenceResponse response = service.createFunction(request);

        assertEquals(10L, response.id());
        assertEquals("Soldador", response.name());
    }

    @Test
    void rejeitaFuncaoDuplicada() {
        CreatePreferenceRequest request = new CreatePreferenceRequest("Eletricista");
        when(functionRepository.findByNameIgnoreCase("Eletricista"))
                .thenReturn(Optional.of(new PrefRole(1, "Eletricista")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createFunction(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void apagaFuncaoExistente() {
        PrefRole entity = new PrefRole(5, "Soldador");
        when(functionRepository.findById(5)).thenReturn(Optional.of(entity));

        service.deleteFunction(5);

        verify(functionRepository).delete(entity);
    }

    @Test
    void erroAoApagarFuncaoInexistente() {
        when(functionRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteFunction(99));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // -------- Competencias --------

    @Test
    void criaCompetencia() {
        when(prefSkillRepository.findByNameIgnoreCase("Soldagem")).thenReturn(Optional.empty());
        when(prefSkillRepository.save(any(PrefSkill.class))).thenAnswer(invocation -> {
            PrefSkill comp = invocation.getArgument(0);
            comp.setId(3);
            return comp;
        });

        CreatePreferenceResponse response = service.createCompetence(new CreatePreferenceRequest("Soldagem"));

        assertEquals(3L, response.id());
        assertEquals("Soldagem", response.name());
    }

    @Test
    void rejeitaCompetenciaDuplicada() {
        when(prefSkillRepository.findByNameIgnoreCase("Soldagem"))
                .thenReturn(Optional.of(new PrefSkill(2, "Soldagem")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createCompetence(new CreatePreferenceRequest("Soldagem")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void apagaCompetencia() {
        PrefSkill comp = new PrefSkill(7, "Caldeiraria");
        when(prefSkillRepository.findById(7)).thenReturn(Optional.of(comp));

        service.deleteCompetence(7);

        verify(prefSkillRepository).delete(comp);
    }

    @Test
    void erroAoApagarCompetenciaInexistente() {
        when(prefSkillRepository.findById(7)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteCompetence(7));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // -------- Areas geograficas --------

    @Test
    void criaAreaGeografica() {
        when(prefGeoAreaRepository.findByNameIgnoreCase("Lisboa")).thenReturn(Optional.empty());
        when(prefGeoAreaRepository.save(any(PrefGeoArea.class))).thenAnswer(invocation -> {
            PrefGeoArea area = invocation.getArgument(0);
            area.setId(4);
            return area;
        });

        CreatePreferenceResponse response = service.createGeoArea(new CreatePreferenceRequest("Lisboa"));

        assertEquals(4L, response.id());
        assertEquals("Lisboa", response.name());
    }

    @Test
    void rejeitaAreaDuplicada() {
        when(prefGeoAreaRepository.findByNameIgnoreCase("Lisboa"))
                .thenReturn(Optional.of(new PrefGeoArea(1, "Lisboa")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createGeoArea(new CreatePreferenceRequest("Lisboa")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void apagaAreaGeografica() {
        PrefGeoArea area = new PrefGeoArea(8, "Porto");
        when(prefGeoAreaRepository.findById(8)).thenReturn(Optional.of(area));

        service.deleteGeoArea(8);

        verify(prefGeoAreaRepository).delete(area);
    }

    @Test
    void erroAoApagarAreaInexistente() {
        when(prefGeoAreaRepository.findById(12)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteGeoArea(12));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // -------- Setores de atividade --------

    @Test
    void criaSetorAtividade() {
        when(prefActivitySectorsRepository.findByNameIgnoreCase("Metalurgia")).thenReturn(Optional.empty());
        when(prefActivitySectorsRepository.save(any(PrefActivitySectors.class))).thenAnswer(invocation -> {
            PrefActivitySectors sector = invocation.getArgument(0);
            sector.setId(9);
            return sector;
        });

        CreatePreferenceResponse response = service.createActivitySector(new CreatePreferenceRequest("Metalurgia"));

        assertEquals(9L, response.id());
        assertEquals("Metalurgia", response.name());
    }

    @Test
    void rejeitaSetorDuplicado() {
        when(prefActivitySectorsRepository.findByNameIgnoreCase("Metalurgia"))
                .thenReturn(Optional.of(new PrefActivitySectors(5, "Metalurgia")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createActivitySector(new CreatePreferenceRequest("Metalurgia")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void apagaSetor() {
        PrefActivitySectors sector = new PrefActivitySectors(11, "Automocao");
        when(prefActivitySectorsRepository.findById(11)).thenReturn(Optional.of(sector));

        service.deleteActivitySector(11);

        verify(prefActivitySectorsRepository).delete(sector);
    }

    @Test
    void erroAoApagarSetorInexistente() {
        when(prefActivitySectorsRepository.findById(15)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteActivitySector(15));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // -------- Validacao geral --------

    @Test
    void rejeitaNomeVazioOuNulo() {
        ResponseStatusException exBlank = assertThrows(ResponseStatusException.class,
                () -> service.createFunction(new CreatePreferenceRequest("   ")));
        assertEquals(HttpStatus.BAD_REQUEST, exBlank.getStatusCode());

        ResponseStatusException exNull = assertThrows(ResponseStatusException.class,
                () -> service.createFunction(null));
        assertEquals(HttpStatus.BAD_REQUEST, exNull.getStatusCode());
    }
}
