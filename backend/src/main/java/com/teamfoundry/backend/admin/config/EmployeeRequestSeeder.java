package com.teamfoundry.backend.admin.config;

import com.teamfoundry.backend.admin.enums.State;
import com.teamfoundry.backend.admin.model.EmployeeRequest;
import com.teamfoundry.backend.admin.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.admin.repository.TeamRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("!test")
public class EmployeeRequestSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRequestSeeder.class);

    @Bean
    @Order(6)
    CommandLineRunner seedEmployeeRequests(EmployeeRequestRepository employeeRequestRepository,
                                           TeamRequestRepository teamRequestRepository) {
        return args -> {
            if (employeeRequestRepository.count() > 0) {
                LOGGER.debug("Employee requests already exist; skipping seeding.");
                return;
            }

            List<EmployeeRequestSeed> seeds = defaultSeeds();
            List<EmployeeRequest> toPersist = new ArrayList<>();

            for (EmployeeRequestSeed seed : seeds) {
                var team = teamRequestRepository.findById(seed.teamRequestId()).orElse(null);
                if (team == null) {
                    LOGGER.warn("TeamRequest {} not found; skipping employee request seed.", seed.teamRequestId());
                    continue;
                }

                int quantity = Math.max(1, seed.quantity());
                for (int i = 0; i < quantity; i++) {
                    EmployeeRequest request = new EmployeeRequest();
                    request.setTeamRequest(team);
                    request.setRequestedRole(seed.requestedRole());
                    request.setState(seed.state());       // INCOMPLETE = pendente
                    request.setAcceptedDate(null);         // sem aceite

                    // Sem funcionário atribuído no seed
                    request.setEmployee(null);

                    toPersist.add(request);
                }
            }

            if (toPersist.isEmpty()) {
                LOGGER.warn("No employee requests were seeded (missing teams).");
                return;
            }

            employeeRequestRepository.saveAll(toPersist);
            LOGGER.info("Seeded {} employee request(s).", toPersist.size());
        };
    }

    private List<EmployeeRequestSeed> defaultSeeds() {
        // Todas pendentes (INCOMPLETE) e sem funcionário
        return List.of(
                new EmployeeRequestSeed(1, "Electricista Sénior", State.INCOMPLETE, 3),
                new EmployeeRequestSeed(2, "Soldador MIG/MAG", State.INCOMPLETE, 2),
                new EmployeeRequestSeed(3, "Técnico de Manutenção", State.INCOMPLETE, 3),
                new EmployeeRequestSeed(4, "Programador PLC", State.INCOMPLETE, 2),
                new EmployeeRequestSeed(5, "Engenheiro de Automação", State.INCOMPLETE, 1),
                new EmployeeRequestSeed(6, "Eletricista Industrial", State.INCOMPLETE, 3),
                new EmployeeRequestSeed(7, "Técnico de SCADA", State.INCOMPLETE, 2),
                new EmployeeRequestSeed(8, "Responsável de Manutenção", State.INCOMPLETE, 3),
                new EmployeeRequestSeed(9, "Caldeireiro Especialista", State.INCOMPLETE, 2)
        );
    }

    private record EmployeeRequestSeed(
            int teamRequestId,
            String requestedRole,
            State state,
            int quantity
    ) {}
}
