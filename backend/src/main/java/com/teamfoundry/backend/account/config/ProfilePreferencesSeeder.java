package com.teamfoundry.backend.account.config;

import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import com.teamfoundry.backend.account.model.preferences.PrefRole;
import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import com.teamfoundry.backend.account.repository.preferences.PrefActivitySectorsRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefSkillRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefRoleRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefGeoAreaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Configuration
@Profile("!test")
public class ProfilePreferencesSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilePreferencesSeeder.class);

    @Bean
    @Order(1)
    CommandLineRunner seedProfileOptions(PrefActivitySectorsRepository prefActivitySectorsRepository,
                                         PrefRoleRepository prefRoleRepository,
                                         PrefSkillRepository prefSkillRepository,
                                         PrefGeoAreaRepository prefGeoAreaRepository) {
        return args -> {
            seedIfEmpty("activity sectors", prefActivitySectorsRepository, defaultActivitySectors());
            seedIfEmpty("job functions", prefRoleRepository, defaultFunctions());
            seedIfEmpty("competences", prefSkillRepository, defaultCompetences());
            seedIfEmpty("geographic areas", prefGeoAreaRepository, defaultGeoAreas());
        };
    }

    private List<PrefActivitySectors> defaultActivitySectors() {
        return List.of(
                activitySector("Fundicao"),
                activitySector("Manutencao Industrial"),
                activitySector("Metalomecanica"),
                activitySector("Automacao"),
                activitySector("Eletronica"),
                activitySector("Energia"),
                activitySector("Infraestruturas"),
                activitySector("Logistica"),
                activitySector("Fabricacao Pesada"),
                activitySector("Robotica")
        );
    }


    private List<PrefRole> defaultFunctions() {
        return List.of(
                prefRole("Eletricista"),
                prefRole("Canalizador"),
                prefRole("Soldador"),
                prefRole("Carpinteiro"),
                prefRole("Pedreiro"),
                prefRole("Mecanico"),
                prefRole("Montador"),
                prefRole("Supervisor"),
                prefRole("Tecnico"),
                prefRole("Operador"),
                prefRole("Inspetor")
        );
    }

        private List<PrefSkill> defaultCompetences() {
        return List.of(
                competence("Lideranca"),
                competence("Comunicacao eficaz"),
                competence("Resolucao de problemas"),
                competence("Pensamento critico"),
                competence("Adaptabilidade"),
                competence("Aprendizado rapido"),
                competence("Gestao de tempo"),
                competence("Trabalho em equipa"),
                competence("Atencao aos detalhes"),
                competence("Tomada de decisao"),
                competence("Gestao de conflitos"),
                competence("Criatividade"),
                competence("Proatividade"),
                competence("Organizacao"),
                competence("Orientacao para resultados")
        );
    }

    private List<PrefGeoArea> defaultGeoAreas() {
        return List.of(
                prefGeoArea("Portugal"),
                prefGeoArea("Espanha"),
                prefGeoArea("Península Ibérica"),
                prefGeoArea("Europa Ocidental"),
                prefGeoArea("Europa Oriental"),
                prefGeoArea("Europa inteira"),
                prefGeoArea("Norte da África")
        );
    }

    private <T> void seedIfEmpty(String label,
                                 JpaRepository<T, Integer> repository,
                                 List<T> entries) {
        if (repository.count() > 0) {
            LOGGER.debug("Skipping {} seeding; repository already contains data.", label);
            return;
        }
        repository.saveAll(entries);
        LOGGER.info("Seeded {} default {} entries.", entries.size(), label);
    }

    private PrefActivitySectors activitySector(String name) {
        PrefActivitySectors sector = new PrefActivitySectors();
        sector.setName(name);
        return sector;
    }

    private PrefRole prefRole(String name) {
        PrefRole function = new PrefRole();
        function.setName(name);
        return function;
    }

    private PrefSkill competence(String name) {
        PrefSkill comp = new PrefSkill();
        comp.setName(name);
        return comp;
    }

    private PrefGeoArea prefGeoArea(String name) {
        PrefGeoArea area = new PrefGeoArea();
        area.setName(name);
        return area;
    }
}

