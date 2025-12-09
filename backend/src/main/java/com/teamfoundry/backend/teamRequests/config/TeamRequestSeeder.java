package com.teamfoundry.backend.teamRequests.config;

import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
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
import java.util.Map;

@Configuration
@Profile("!test")
public class TeamRequestSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamRequestSeeder.class);

    @Bean
    @Order(5)
    CommandLineRunner seedTeamRequests(TeamRequestRepository teamRequestRepository,
                                       CompanyAccountRepository companyAccountRepository,
                                       AdminAccountRepository adminAccountRepository) {
        return args -> {
            if (teamRequestRepository.count() > 0) {
                LOGGER.debug("Team requests already exist; skipping seeding.");
                return;
            }

            Map<String, Integer> adminIds = adminAccountRepository.findAll().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            a -> a.getUsername().toLowerCase(),
                            a -> a.getId()
                    ));

            List<TeamRequest> toPersist = new ArrayList<>();
            for (TeamRequestSeed seed : defaultSeeds()) {
                CompanyAccount company = companyAccountRepository.findByEmail(seed.companyEmail()).orElse(null);
                if (company == null) {
                    LOGGER.warn("Company {} not found; skipping team request seed for {}.",
                            seed.companyEmail(), seed.teamName());
                    continue;
                }

                TeamRequest request = new TeamRequest();
                request.setCompany(company);
                request.setTeamName(seed.teamName());
                request.setDescription(seed.description());
                request.setLocation(seed.location());
                request.setState(seed.state());
                Integer adminId = seed.responsibleAdminUsername() == null ? null :
                        adminIds.get(seed.responsibleAdminUsername().toLowerCase());
                request.setResponsibleAdminId(adminId);
                request.setStartDate(seed.startDate());
                request.setEndDate(seed.endDate());
                request.setCreatedAt(seed.createdAt());
                toPersist.add(request);
            }

            if (toPersist.isEmpty()) {
                LOGGER.warn("No team requests were seeded (no matching companies).");
                return;
            }

            teamRequestRepository.saveAll(toPersist);
            LOGGER.info("Seeded {} team request(s).", toPersist.size());
        };
    }

    private List<TeamRequestSeed> defaultSeeds() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                // Ativos incompletos
                new TeamRequestSeed("contact@blueorbitlabs.com", "Equipe Retrofit Norte",
                        "Retrofit elétrico e solda leve.", "Lisboa", State.INCOMPLETE, "admin1",
                        now.plusDays(7), now.plusDays(30), now.minusDays(2)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Task force Soldagem",
                        "Soldagem MIG/MAG estrutural.", "Porto", State.INCOMPLETE, "admin2",
                        now.plusDays(5), now.plusDays(25), now.minusDays(1)),
                new TeamRequestSeed("talent@atlantic-robotics.eu", "Montagem Industrial Sul",
                        "Montagem mecânica e canalização.", "Faro", State.INCOMPLETE, "admin3",
                        now.plusDays(10), now.plusDays(40), now.minusDays(3)),
                // Concluídos
                new TeamRequestSeed("contact@blueorbitlabs.com", "Linha Robotizada A",
                        "Instalação de célula robotizada.", "Lisboa", State.COMPLETE, "admin1",
                        now.minusDays(60), now.minusDays(30), now.minusDays(70)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Squad SCADA Norte",
                        "Rollout SCADA nas subestações.", "Porto", State.COMPLETE, "admin2",
                        now.minusDays(50), now.minusDays(20), now.minusDays(55)),
                new TeamRequestSeed("hr@iberiapower.com", "Equipa Solar Oeste",
                        "Task force O&M solar.", "Braga", State.COMPLETE, "admin4",
                        now.minusDays(40), now.minusDays(10), now.minusDays(45))
        );
    }

    private record TeamRequestSeed(
            String companyEmail,
            String teamName,
            String description,
            String location,
            State state,
            String responsibleAdminUsername,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime createdAt
    ) {
    }
}
