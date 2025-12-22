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
            Map<String, Integer> adminIds = adminAccountRepository.findAll().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            a -> a.getUsername().toLowerCase(),
                            a -> a.getId()
                    ));

            List<TeamRequest> toPersist = new ArrayList<>();
            for (TeamRequestSeed seed : defaultSeeds()) {
                if (teamRequestRepository.findByTeamName(seed.teamName()).isPresent()) {
                    LOGGER.debug("Team request {} already exists; skipping seed.", seed.teamName());
                    continue;
                }
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
                // --- FerroMec (Existing) ---
                new TeamRequestSeed("operacoes@ferromec.pt", "Squad Retrofit Alfa",
                        "Equipe para retrofit e soldagem leve.", "Porto", State.INCOMPLETE, "admin1",
                        now.plusDays(7), now.plusDays(35), now.minusDays(3)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Equipe Manutencao Norte",
                        "Manutencao preventiva em linha industrial.", "Braga", State.COMPLETED, "admin2",
                        now.minusMonths(2), now.minusMonths(1), now.minusMonths(3)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Time Soldagem Linha 2",
                        "Soldagem estrutural em linha de producao.", "Aveiro", State.INCOMPLETE, "admin3",
                        now.plusDays(12), now.plusDays(45), now.minusDays(4)),

                // --- Blue Orbit Labs ---
                new TeamRequestSeed("contact@blueorbitlabs.com", "Equipe Analytics Beta",
                        "Implementação de sensores IoT para analise de dados.", "Lisboa", State.COMPLETED, "admin1",
                        now.minusMonths(5), now.minusMonths(2), now.minusMonths(6)),
                new TeamRequestSeed("contact@blueorbitlabs.com", "Squad DevOps Core",
                        "Infraestrutura de CI/CD para nova plataforma.", "Remoto", State.INCOMPLETE, "admin4",
                        now.plusDays(5), now.plusMonths(2), now.minusDays(2)),

                // --- NovaLink Automation ---
                new TeamRequestSeed("contato@novalink-automation.com", "Celula Robotica X1",
                        "Montagem de celula robotizada para linha automovel.", "Palmela", State.COMPLETED, "admin2",
                        now.minusMonths(8), now.minusMonths(4), now.minusMonths(9)),
                new TeamRequestSeed("contato@novalink-automation.com", "Manutencao Sensores",
                        "Calibracao de sensores opticos em ambiente fabril.", "Aveiro", State.INCOMPLETE, "admin5",
                        now.plusDays(10), now.plusDays(20), now.minusDays(1)),

                // --- Iberia Power Systems ---
                new TeamRequestSeed("hr@iberiapower.com", "Upgrade Subestacao Madrid",
                        "Atualizacao de transformadores de alta tensao.", "Madrid", State.COMPLETED, "admin3",
                        now.minusYears(1), now.minusMonths(10), now.minusYears(1).minusMonths(1)),
                new TeamRequestSeed("hr@iberiapower.com", "Equipe Eolica Offshore",
                        "Manutencao preventiva em parque eolico flutuante.", "Viana do Castelo", State.INCOMPLETE, "admin6",
                        now.plusWeeks(2), now.plusMonths(3), now.minusDays(5)),

                // --- Atlantic Dynamics ---
                new TeamRequestSeed("talent@atlantic-dynamics.eu", "Montagem Hidraulica Pesada",
                        "Sistema hidraulico para guindastes portuarios.", "Sines", State.COMPLETED, "admin4",
                        now.minusMonths(3), now.minusMonths(1), now.minusMonths(4)),

                // --- More Active/Incomplete for diversity ---
                new TeamRequestSeed("operacoes@ferromec.pt", "Equipe Montagem Sul",
                        "Montagem mecanica e ajustes finais.", "Faro", State.INCOMPLETE, "admin4",
                        now.plusDays(9), now.plusDays(38), now.minusDays(1)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Squad Eletrica Delta",
                        "Instalacoes eletricas e testes de painel.", "Lisboa", State.INCOMPLETE, "admin5",
                        now.plusDays(14), now.plusDays(50), now.minusDays(5)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Time Canalizacao Oeste",
                        "Canalizacao industrial e suportes.", "Leiria", State.INCOMPLETE, "admin6",
                        now.plusDays(6), now.plusDays(32), now.minusDays(2)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Equipe Automacao Leste",
                        "Automacao de celulas e integracao de sensores.", "Coimbra", State.INCOMPLETE, "admin7",
                        now.plusDays(11), now.plusDays(42), now.minusDays(3)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Time Inspecao 3D",
                        "Inspecao dimensional e controle de qualidade.", "Setubal", State.INCOMPLETE, "admin8",
                        now.plusDays(8), now.plusDays(36), now.minusDays(1)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Squad Logistica Turno B",
                        "Apoio logistico e movimentacao interna.", "Vila Nova de Gaia", State.INCOMPLETE, "admin9",
                        now.plusDays(15), now.plusDays(55), now.minusDays(6)),
                new TeamRequestSeed("operacoes@ferromec.pt", "Equipe Montagem Beta",
                        "Montagem de subconjuntos mecanicos.", "Porto", State.INCOMPLETE, "admin10",
                        now.plusDays(10), now.plusDays(40), now.minusDays(2))
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
