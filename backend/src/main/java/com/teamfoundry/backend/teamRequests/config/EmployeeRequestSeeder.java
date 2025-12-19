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
        List<EmployeeRequestSeed> seeds = new ArrayList<>();

        addOpenRequests(seeds, "Squad Retrofit Alfa", 7, now.minusDays(2), new BigDecimal("1750"));
        addOpenRequests(seeds, "Equipe Manutencao Norte", 8, now.minusDays(2), new BigDecimal("1650"));
        addOpenRequests(seeds, "Time Soldagem Linha 2", 9, now.minusDays(3), new BigDecimal("1700"));
        addOpenRequests(seeds, "Equipe Montagem Sul", 10, now.minusDays(1), new BigDecimal("1600"));
        addOpenRequests(seeds, "Squad Eletrica Delta", 11, now.minusDays(4), new BigDecimal("1900"));
        addOpenRequests(seeds, "Time Canalizacao Oeste", 12, now.minusDays(3), new BigDecimal("1680"));
        addOpenRequests(seeds, "Equipe Automacao Leste", 13, now.minusDays(5), new BigDecimal("2000"));
        addOpenRequests(seeds, "Time Inspecao 3D", 14, now.minusDays(2), new BigDecimal("1750"));
        addOpenRequests(seeds, "Squad Logistica Turno B", 15, now.minusDays(6), new BigDecimal("1550"));
        addOpenRequests(seeds, "Equipe Montagem Beta", 7, now.minusDays(1), new BigDecimal("1620"));

        return seeds;
    }

    private void addOpenRequests(List<EmployeeRequestSeed> seeds,
                                 String teamName,
                                 int count,
                                 LocalDateTime createdAt,
                                 BigDecimal baseSalary) {
        String[] roles = {
                "Eletricista",
                "Soldador",
                "Canalizador",
                "Carpinteiro",
                "Mecanico",
                "Montador",
                "Supervisor",
                "Tecnico",
                "Operador",
                "Inspetor"
        };

        for (int i = 0; i < count; i++) {
            String role = roles[i % roles.length];
            BigDecimal salary = baseSalary.add(BigDecimal.valueOf(50L * (i % 4)));
            seeds.add(new EmployeeRequestSeed(teamName, role, null, null, createdAt, salary));
        }
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
