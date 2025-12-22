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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            List<EmployeeRequestSeed> seeds = defaultSeeds();
            List<EmployeeRequest> toPersist = new ArrayList<>();

            Map<String, List<EmployeeRequestSeed>> seedsByTeam = seeds.stream()
                    .collect(Collectors.groupingBy(EmployeeRequestSeed::teamName));

            for (Map.Entry<String, List<EmployeeRequestSeed>> entry : seedsByTeam.entrySet()) {
                String teamName = entry.getKey();
                List<EmployeeRequestSeed> teamSeeds = entry.getValue();
                TeamRequest team = teamRequestRepository.findByTeamName(teamName).orElse(null);
                if (team == null) {
                    LOGGER.warn("TeamRequest {} not found; skipping employee request seed.", teamName);
                    continue;
                }

                // 1. Process Open Seeds (No Employee assigned)
                List<EmployeeRequestSeed> openSeeds = teamSeeds.stream()
                        .filter(seed -> seed.employeeEmail() == null)
                        .collect(Collectors.toList());
                if (!openSeeds.isEmpty()) {
                    long existingOpen = employeeRequestRepository.countByTeamRequest_IdAndEmployeeIsNull(team.getId());
                    // Simply add all defined open seeds, as we might want specific roles multiple times.
                    // The logic 'startIndex' was to prevent duplicates if running multiple times without cleanup, 
                    // but we are assuming clean DB or specific specific intent.
                    // Let's stick to adding them if they don't exist? 
                    // To be safe and simple for this 'fix': just add them. The initialization is usually on empty DB.
                    
                    for (EmployeeRequestSeed seed : openSeeds) {
                        EmployeeRequest request = new EmployeeRequest();
                        request.setTeamRequest(team);
                        request.setRequestedRole(seed.requestedRole());
                        request.setRequestedSalary(seed.salary());
                        request.setCreatedAt(seed.createdAt());
                        toPersist.add(request);
                    }
                }

                // 2. Process Assigned Seeds (Employee assigned)
                for (EmployeeRequestSeed seed : teamSeeds) {
                    if (seed.employeeEmail() == null) {
                        continue;
                    }
                    Optional<EmployeeAccount> employee = employeeAccountRepository.findByEmail(seed.employeeEmail());
                    if (employee.isEmpty()) {
                        LOGGER.warn("Employee {} not found; leaving request {} without employee.", seed.employeeEmail(), teamName);
                        continue;
                    }
                    if (employeeRequestRepository.countAcceptedForTeam(team.getId(), employee.get().getId()) > 0) {
                        LOGGER.debug("Employee {} already accepted for {}; skipping seed.", seed.employeeEmail(), teamName);
                        continue;
                    }
                    EmployeeRequest request = new EmployeeRequest();
                    request.setTeamRequest(team);
                    request.setRequestedRole(seed.requestedRole());
                    request.setRequestedSalary(seed.salary());
                    request.setCreatedAt(seed.createdAt());
                    request.setEmployee(employee.get());
                    request.setAcceptedDate(seed.acceptedDate());
                    toPersist.add(request);
                }
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

        // --- Active Requests (FerroMec) ---
        addOpenRequests(seeds, "Squad Retrofit Alfa", 7, now.minusDays(2), new BigDecimal("1750"));
        // Ensuring roles for offers exist (Eletricista, Soldador, Mecanico are in the first 7 of roles array)
        
        addOpenRequests(seeds, "Time Soldagem Linha 2", 9, now.minusDays(3), new BigDecimal("1700"));
        addOpenRequests(seeds, "Equipe Montagem Sul", 10, now.minusDays(1), new BigDecimal("1600"));
        addOpenRequests(seeds, "Squad Eletrica Delta", 11, now.minusDays(4), new BigDecimal("1900"));
        addOpenRequests(seeds, "Time Canalizacao Oeste", 12, now.minusDays(3), new BigDecimal("1680"));
        addOpenRequests(seeds, "Equipe Automacao Leste", 13, now.minusDays(5), new BigDecimal("2000"));
        addOpenRequests(seeds, "Time Inspecao 3D", 14, now.minusDays(2), new BigDecimal("1750"));
        addOpenRequests(seeds, "Squad Logistica Turno B", 15, now.minusDays(6), new BigDecimal("1550"));
        addOpenRequests(seeds, "Equipe Montagem Beta", 7, now.minusDays(1), new BigDecimal("1620"));

        // --- Active Requests (Others) - Explicitly adding needed roles for offers ---
        // Squad DevOps Core (Blue Orbit): Needs Supervisor (Active), Tecnico (Active)
        seeds.add(new EmployeeRequestSeed("Squad DevOps Core", "Supervisor", null, null, now.minusDays(2), new BigDecimal("2800")));
        seeds.add(new EmployeeRequestSeed("Squad DevOps Core", "Tecnico", null, null, now.minusDays(2), new BigDecimal("2500")));
        addOpenRequests(seeds, "Squad DevOps Core", 3, now.minusDays(2), new BigDecimal("2500"));

        // Manutencao Sensores (NovaLink): Needs Inspetor (Active)
        seeds.add(new EmployeeRequestSeed("Manutencao Sensores", "Inspetor", null, null, now.minusDays(1), new BigDecimal("1950")));
        addOpenRequests(seeds, "Manutencao Sensores", 4, now.minusDays(1), new BigDecimal("1800"));

        // Equipe Eolica Offshore (Iberia)
        addOpenRequests(seeds, "Equipe Eolica Offshore", 6, now.minusDays(4), new BigDecimal("2200"));


        // --- HISTORICAL: Equipe Manutencao Norte (FerroMec, 2 months ago) ---
        // Matches OfferSeeder Winners: Miguel Santos (Eletricista), Tiago Rocha (Mecanico)
        seeds.add(new EmployeeRequestSeed("Equipe Manutencao Norte", "Eletricista", "miguel.santos@teamfoundry.com", now.minusMonths(2).plusDays(2), now.minusMonths(3), new BigDecimal("1650")));
        seeds.add(new EmployeeRequestSeed("Equipe Manutencao Norte", "Mecanico", "tiago.rocha@teamfoundry.com", now.minusMonths(2).plusDays(1), now.minusMonths(3), new BigDecimal("1650")));
        // Extra filled position?
        seeds.add(new EmployeeRequestSeed("Equipe Manutencao Norte", "Canalizador", "maria.sousa@teamfoundry.com", now.minusMonths(2).plusDays(3), now.minusMonths(3), new BigDecimal("1650")));

        // --- HISTORICAL: Equipe Analytics Beta (Blue Orbit, 5 months ago) ---
        // No offers seeded for this one in OfferSeeder, but let's leave existing data for robustness
        seeds.add(new EmployeeRequestSeed("Equipe Analytics Beta", "Tecnico", "ricardo.pires@teamfoundry.com", now.minusMonths(5).plusDays(5), now.minusMonths(6), new BigDecimal("1900")));
        seeds.add(new EmployeeRequestSeed("Equipe Analytics Beta", "Operador", "ana.martins@teamfoundry.com", now.minusMonths(5).plusDays(2), now.minusMonths(6), new BigDecimal("2100"))); 

        // --- HISTORICAL: Celula Robotica X1 (NovaLink, 8 months ago) ---
        // Matches OfferSeeder Winners: Bruno Martins (Soldador), Hugo Almeida (Montador)
        seeds.add(new EmployeeRequestSeed("Celula Robotica X1", "Soldador", "bruno.martins@teamfoundry.com", now.minusMonths(8).plusDays(10), now.minusMonths(9), new BigDecimal("1850")));
        seeds.add(new EmployeeRequestSeed("Celula Robotica X1", "Montador", "hugo.almeida@teamfoundry.com", now.minusMonths(8).plusDays(12), now.minusMonths(9), new BigDecimal("1750")));
        // Extra
        seeds.add(new EmployeeRequestSeed("Celula Robotica X1", "Mecanico", "andre.almeida@teamfoundry.com", now.minusMonths(8).plusDays(15), now.minusMonths(9), new BigDecimal("1800")));

        // --- HISTORICAL: Upgrade Subestacao Madrid (Iberia Power, 1 year ago) ---
        // Matches OfferSeeder Winners: Pedro Ferreira (Eletricista)
        seeds.add(new EmployeeRequestSeed("Upgrade Subestacao Madrid", "Eletricista", "pedro.ferreira@teamfoundry.com", now.minusYears(1).plusDays(5), now.minusYears(1).minusMonths(1), new BigDecimal("2300")));
        // Extra
        seeds.add(new EmployeeRequestSeed("Upgrade Subestacao Madrid", "Supervisor", "rui.gomes@teamfoundry.com", now.minusYears(1).plusDays(1), now.minusYears(1).minusMonths(1), new BigDecimal("2800")));

        // --- HISTORICAL: Montagem Hidraulica Pesada (Atlantic, 3 months ago) ---
        // Matches OfferSeeder Winners: Andre Gomes (Mecanico)
        seeds.add(new EmployeeRequestSeed("Montagem Hidraulica Pesada", "Mecanico", "andre.gomes@teamfoundry.com", now.minusMonths(3).plusDays(4), now.minusMonths(4), new BigDecimal("1950")));
        // Extra
        seeds.add(new EmployeeRequestSeed("Montagem Hidraulica Pesada", "Soldador", "patricia.medeiros@teamfoundry.com", now.minusMonths(3).plusDays(6), now.minusMonths(4), new BigDecimal("1950")));

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
