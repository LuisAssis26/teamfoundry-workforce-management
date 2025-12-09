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
                                  EmployeeAccountRepository employeeAccountRepository) {
        return args -> {
            if (inviteRepository.count() > 0) {
                LOGGER.debug("Invites already exist; skipping seeding.");
                return;
            }

            List<InviteSeed> seeds = defaultSeeds();
            List<EmployeeRequestOffer> toSave = new ArrayList<>();

            for (InviteSeed seed : seeds) {
                Optional<EmployeeRequest> requestOpt = employeeRequestRepository.findById(seed.requestId());
                Optional<EmployeeAccount> employeeOpt = employeeAccountRepository.findByEmail(seed.employeeEmail());
                if (requestOpt.isEmpty() || employeeOpt.isEmpty()) {
                    LOGGER.warn("Invite seed skipped (request or employee not found): {} -> {}", seed.requestId(), seed.employeeEmail());
                    continue;
                }
                EmployeeRequestOffer invite = new EmployeeRequestOffer();
                invite.setEmployeeRequest(requestOpt.get());
                invite.setEmployee(employeeOpt.get());
                invite.setActive(true);
                toSave.add(invite);
            }

            if (toSave.isEmpty()) {
                LOGGER.warn("No invites were seeded.");
                return;
            }

            inviteRepository.saveAll(toSave);
            LOGGER.info("Seeded {} invites.", toSave.size());
        };
    }

    private List<InviteSeed> defaultSeeds() {
        return List.of(
                // Retrofit Norte
                new InviteSeed(1, "joao.silva@teamfoundry.com"),
                new InviteSeed(1, "sofia.lima@teamfoundry.com"),
                new InviteSeed(2, "daniel.matos@teamfoundry.com"),
                new InviteSeed(3, "tiago.rocha@teamfoundry.com"),
                // Task force Soldagem
                new InviteSeed(5, "patricia.medeiros@teamfoundry.com"),
                new InviteSeed(6, "carlos.oliveira@teamfoundry.com"),
                // Montagem Industrial Sul
                new InviteSeed(8, "carla.ferreira@teamfoundry.com"),
                new InviteSeed(9, "marta.ribeiro@teamfoundry.com"),
                new InviteSeed(10, "ricardo.pires@teamfoundry.com")
        );
    }

    private record InviteSeed(int requestId, String employeeEmail) {}
}
