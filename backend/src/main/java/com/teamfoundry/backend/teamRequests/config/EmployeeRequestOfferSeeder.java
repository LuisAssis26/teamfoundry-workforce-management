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

            // Define seeds using Team Name + Role + Candidate Email
            List<SmartInviteSeed> seeds = List.of(
                    // Retrofit Norte - Eletricista
                    new SmartInviteSeed("Equipe Retrofit Norte", "Eletricista", "joao.silva@teamfoundry.com"),
                    new SmartInviteSeed("Equipe Retrofit Norte", "Eletricista", "sofia.lima@teamfoundry.com"),
                    // Retrofit Norte - Eletricista (2nd slot - just to sure)
                    new SmartInviteSeed("Equipe Retrofit Norte", "Eletricista", "daniel.matos@teamfoundry.com"),
                    // Retrofit Norte - Soldador
                    new SmartInviteSeed("Equipe Retrofit Norte", "Soldador", "tiago.rocha@teamfoundry.com")
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
                        .findByTeamRequest_IdAndRequestedRoleIgnoreCaseAndEmployeeIsNull(teamId, seed.role());
                
                if (requests.isEmpty()) {
                    LOGGER.warn("Seeder: No open requests for '{}' in team '{}'", seed.role(), seed.teamName());
                    continue;
                }
                
                // Use the first available request (or any, logic doesn't strictly matter for seeding invites, 
                // but ideally we attach to one that isn't already "full" of invites? 
                // Actually, one request can have multiple invites. We just pick the first one.)
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
                invite.setActive(true);
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

    private record SmartInviteSeed(String teamName, String role, String employeeEmail) {}


}
