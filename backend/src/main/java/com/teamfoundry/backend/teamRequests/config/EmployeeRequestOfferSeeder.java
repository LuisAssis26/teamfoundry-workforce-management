package com.teamfoundry.backend.teamRequests.config;

import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequestOffer;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestOfferRepository;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@Profile("!test")
public class EmployeeRequestOfferSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRequestOfferSeeder.class);

    @Bean
    @Order(7)
    CommandLineRunner seedInvites(EmployeeRequestOfferRepository inviteRepository,
                                  EmployeeRequestRepository employeeRequestRepository,
                                  com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository teamRequestRepository,
                                  EmployeeAccountRepository employeeAccountRepository) {
        return args -> {
            if (inviteRepository.count() > 0) {
                LOGGER.debug("Invites already exist; skipping seeding.");
                return;
            }

            // Define seeds using Team Name + Role + Candidate Email + Active Status
            List<SmartInviteSeed> seeds = List.of(
                    // --- ACTIVE OFFERS (Pending/Active) ---
                    // Squad Retrofit Alfa (FerroMec - Active)
                    new SmartInviteSeed("Squad Retrofit Alfa", "Eletricista", "joao.silva@teamfoundry.com", true),
                    new SmartInviteSeed("Squad Retrofit Alfa", "Soldador", "carlos.oliveira@teamfoundry.com", true),
                    new SmartInviteSeed("Squad Retrofit Alfa", "Mecanico", "tiago.rocha@teamfoundry.com", true),

                    // Squad DevOps Core (Blue Orbit - Active)
                    new SmartInviteSeed("Squad DevOps Core", "Supervisor", "joao.silva@teamfoundry.com", true),
                    new SmartInviteSeed("Squad DevOps Core", "Tecnico", "sofia.lima@teamfoundry.com", true),

                    // Manutencao Sensores (NovaLink - Active)
                    new SmartInviteSeed("Manutencao Sensores", "Inspetor", "ana.costa@teamfoundry.com", true),

                    // --- HISTORICAL OFFERS (Completed/Rejected/Expired) ---
                    // Equipe Manutencao Norte (FerroMec - Completed)
                    new SmartInviteSeed("Equipe Manutencao Norte", "Eletricista", "sofia.lima@teamfoundry.com", false), // Rejected
                    new SmartInviteSeed("Equipe Manutencao Norte", "Eletricista", "miguel.santos@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Equipe Manutencao Norte", "Mecanico", "tiago.rocha@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Equipe Manutencao Norte", "Mecanico", "diogo.pereira@teamfoundry.com", false), // Rejected
                    new SmartInviteSeed("Equipe Manutencao Norte", "Canalizador", "joao.silva@teamfoundry.com", false), // Extra Rejected

                    // Celula Robotica X1 (NovaLink - Completed)
                    new SmartInviteSeed("Celula Robotica X1", "Soldador", "carlos.oliveira@teamfoundry.com", false), // Rejected
                    new SmartInviteSeed("Celula Robotica X1", "Soldador", "bruno.martins@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Celula Robotica X1", "Montador", "hugo.almeida@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Celula Robotica X1", "Montador", "ana.martins@teamfoundry.com", false), // Extra Rejected

                    // Equipe Analytics Beta (Blue Orbit - Completed)
                    new SmartInviteSeed("Equipe Analytics Beta", "Tecnico", "ricardo.pires@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Equipe Analytics Beta", "Operador", "ana.martins@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Equipe Analytics Beta", "Tecnico", "joao.silva@teamfoundry.com", false), // Rejected
                    new SmartInviteSeed("Equipe Analytics Beta", "Operador", "sofia.lima@teamfoundry.com", false), // Extra Rejected

                    // Upgrade Subestacao Madrid (Iberia Power - Completed)
                    new SmartInviteSeed("Upgrade Subestacao Madrid", "Eletricista", "joao.silva@teamfoundry.com", false), // Rejected
                    new SmartInviteSeed("Upgrade Subestacao Madrid", "Eletricista", "pedro.ferreira@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Upgrade Subestacao Madrid", "Supervisor", "tiago.rocha@teamfoundry.com", false), // Extra Rejected (requires request for Supervisor in RequestSeeder, which exists)

                    // Montagem Hidraulica Pesada (Atlantic Dynamics - Completed)
                    new SmartInviteSeed("Montagem Hidraulica Pesada", "Mecanico", "tiago.rocha@teamfoundry.com", false), // Rejected
                    new SmartInviteSeed("Montagem Hidraulica Pesada", "Mecanico", "andre.gomes@teamfoundry.com", true), // Accepted
                    new SmartInviteSeed("Montagem Hidraulica Pesada", "Soldador", "miguel.santos@teamfoundry.com", false) // Extra Rejected (Matches Soldador request in RequestSeeder)
            );

            List<EmployeeRequestOffer> toSave = new ArrayList<>();

            for (SmartInviteSeed seed : seeds) {
                // 1. Find the Team
                var teamOpt = teamRequestRepository.findByTeamName(seed.teamName());
                if (teamOpt.isEmpty()) {
                    LOGGER.warn("Seeder: Team '{}' not found", seed.teamName());
                    continue;
                }
                Integer teamId = teamOpt.get().getId();

                // 2. Find an available request for the role in this team
                List<EmployeeRequest> requests = employeeRequestRepository
                        .findByTeamRequest_IdAndRequestedRoleIgnoreCase(teamId, seed.role());
                
                if (requests.isEmpty()) {
                    LOGGER.warn("Seeder: No requests for '{}' in team '{}'", seed.role(), seed.teamName());
                    continue;
                }
                
                // Pick the first one (simplification: attach to any matching role request)
                EmployeeRequest targetRequest = requests.get(0);

                // 3. Find the Employee
                Optional<EmployeeAccount> employeeOpt = employeeAccountRepository.findByEmail(seed.employeeEmail());
                if (employeeOpt.isEmpty()) {
                    LOGGER.warn("Seeder: Employee '{}' not found", seed.employeeEmail());
                    continue;
                }

                // 4. Create Invite
                EmployeeRequestOffer invite = new EmployeeRequestOffer();
                invite.setEmployeeRequest(targetRequest);
                invite.setEmployee(employeeOpt.get());
                invite.setActive(seed.active());
                toSave.add(invite);
            }

            if (!toSave.isEmpty()) {
                inviteRepository.saveAll(toSave);
                LOGGER.info("Seeded {} invites via Smart Seeder.", toSave.size());
            } else {
                LOGGER.warn("Smart Seeder generated 0 invites.");
            }
        };
    }

    private record SmartInviteSeed(String teamName, String role, String employeeEmail, boolean active) {}

}
