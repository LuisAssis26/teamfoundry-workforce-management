package com.teamfoundry.backend.teamRequests.config;

import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@Profile("!test")
public class EmployeeRequestSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRequestSeeder.class);

    @Bean
    @Order(6)
    CommandLineRunner seedEmployeeRequests(EmployeeRequestRepository employeeRequestRepository,
                                           TeamRequestRepository teamRequestRepository,
                                           EmployeeAccountRepository employeeAccountRepository) {
        return args -> {
            if (employeeRequestRepository.count() > 0) {
                LOGGER.debug("Employee requests already exist; skipping seeding.");
                return;
            }

            List<EmployeeRequestSeed> seeds = defaultSeeds();
            List<EmployeeRequest> toPersist = new ArrayList<>();

            for (EmployeeRequestSeed seed : seeds) {
                TeamRequest team = teamRequestRepository.findByTeamName(seed.teamName()).orElse(null);
                if (team == null) {
                    LOGGER.warn("TeamRequest {} not found; skipping employee request seed.", seed.teamName());
                    continue;
                }

                EmployeeRequest request = new EmployeeRequest();
                request.setTeamRequest(team);
                request.setRequestedRole(seed.requestedRole());
                request.setRequestedSalary(seed.salary());
                request.setCreatedAt(seed.createdAt());

                if (seed.employeeEmail() != null) {
                    Optional<EmployeeAccount> employee = employeeAccountRepository.findByEmail(seed.employeeEmail());
                    if (employee.isPresent()) {
                        request.setEmployee(employee.get());
                        request.setAcceptedDate(seed.acceptedDate());
                    } else {
                        LOGGER.warn("Employee {} not found; leaving request {} without employee.", seed.employeeEmail(), seed.teamName());
                    }
                }

                toPersist.add(request);
            }

            if (toPersist.isEmpty()) {
                LOGGER.warn("No employee requests were seeded (missing teams/employees).");
                return;
            }

            employeeRequestRepository.saveAll(toPersist);
            LOGGER.info("Seeded {} employee request(s).", toPersist.size());
        };
    }

    private List<EmployeeRequestSeed> defaultSeeds() {
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                // Equipe Retrofit Norte (INCOMPLETE, admin1) -> vagas abertas
                new EmployeeRequestSeed("Equipe Retrofit Norte", "Eletricista", null, null, now.minusDays(1), new BigDecimal("1800")),
                new EmployeeRequestSeed("Equipe Retrofit Norte", "Eletricista", null, null, now.minusDays(1), new BigDecimal("1800")),
                new EmployeeRequestSeed("Equipe Retrofit Norte", "Soldador", null, null, now.minusDays(1), new BigDecimal("1700")),
                new EmployeeRequestSeed("Equipe Retrofit Norte", "Canalizador", null, null, now.minusDays(1), new BigDecimal("1600")),

                // Task force Soldagem (INCOMPLETE, admin2) -> abertas
                new EmployeeRequestSeed("Task force Soldagem", "Soldador", null, null, now.minusDays(2), new BigDecimal("1750")),
                new EmployeeRequestSeed("Task force Soldagem", "Soldador", null, null, now.minusDays(2), new BigDecimal("1750")),
                new EmployeeRequestSeed("Task force Soldagem", "Carpinteiro", null, null, now.minusDays(2), new BigDecimal("1500")),

                // Montagem Industrial Sul (INCOMPLETE, admin3) -> abertas
                new EmployeeRequestSeed("Montagem Industrial Sul", "Canalizador", null, null, now.minusDays(3), new BigDecimal("1650")),
                new EmployeeRequestSeed("Montagem Industrial Sul", "Canalizador", null, null, now.minusDays(3), new BigDecimal("1650")),
                new EmployeeRequestSeed("Montagem Industrial Sul", "Eletricista", null, null, now.minusDays(3), new BigDecimal("1800")),

                // Linha Robotizada A (COMPLETE) -> preenchidas
                new EmployeeRequestSeed("Linha Robotizada A", "Eletricista", "joao.silva@teamfoundry.com", now.minusDays(50), now.minusDays(55), new BigDecimal("1900")),
                new EmployeeRequestSeed("Linha Robotizada A", "Soldador", "tiago.rocha@teamfoundry.com", now.minusDays(48), now.minusDays(52), new BigDecimal("1850")),
                new EmployeeRequestSeed("Linha Robotizada A", "Carpinteiro", "joana.pereira@teamfoundry.com", now.minusDays(46), now.minusDays(50), new BigDecimal("1600")),

                // Squad SCADA Norte (COMPLETE) -> preenchidas
                new EmployeeRequestSeed("Squad SCADA Norte", "Eletricista", "daniel.matos@teamfoundry.com", now.minusDays(35), now.minusDays(38), new BigDecimal("1950")),
                new EmployeeRequestSeed("Squad SCADA Norte", "Soldador", "patricia.medeiros@teamfoundry.com", now.minusDays(34), now.minusDays(37), new BigDecimal("1800")),

                // Equipa Solar Oeste (COMPLETE) -> preenchidas
                new EmployeeRequestSeed("Equipa Solar Oeste", "Canalizador", "carla.ferreira@teamfoundry.com", now.minusDays(25), now.minusDays(28), new BigDecimal("1700")),
                new EmployeeRequestSeed("Equipa Solar Oeste", "Eletricista", "sofia.lima@teamfoundry.com", now.minusDays(24), now.minusDays(27), new BigDecimal("1850"))
        );
    }

    private record EmployeeRequestSeed(
            String teamName,
            String requestedRole,
            String employeeEmail,
            LocalDateTime acceptedDate,
            LocalDateTime createdAt,
            BigDecimal salary
    ) {}
}
