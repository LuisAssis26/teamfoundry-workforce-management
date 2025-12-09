package com.teamfoundry.backend.account.config.company;

import com.teamfoundry.backend.account.model.company.CompanyAccountManager;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
@Profile("!test")
public class CompanyAccountManagerInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAccountManagerInitializer.class);

    @Bean
    @Order(4)
    CommandLineRunner seedCompanyManagers(CompanyAccountRepository companyAccountRepository,
                                          CompanyAccountOwnerRepository ownerRepository,
                                          TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            for (ManagerSeed seed : defaultManagerSeeds()) {
                companyAccountRepository.findByEmail(seed.companyEmail()).ifPresentOrElse(company -> {
                            if (ownerRepository.existsById(company.getId())) {
                                LOGGER.debug("Manager for company {} already exists; skipping.", company.getEmail());
                                return;
                            }

                            CompanyAccountManager manager = new CompanyAccountManager();
                            manager.setCompanyAccount(company);
                            manager.setEmail(seed.email());
                            manager.setName(seed.name());
                            manager.setPhone(seed.phone());
                            manager.setPosition(seed.position());

                            ownerRepository.save(manager);
                            LOGGER.info("Seeded manager {} for company {}.", manager.getEmail(), company.getEmail());
                        },
                        () -> LOGGER.warn("Company {} not found; skipping manager seeding.", seed.companyEmail()));
            }
        });
    }

    private List<ManagerSeed> defaultManagerSeeds() {
        return List.of(
                new ManagerSeed("contact@blueorbitlabs.com", "helena.sousa@blueorbitlabs.com",
                        "Helena Sousa", "+351919000123", "Head of Workforce Planning"),
                new ManagerSeed("operacoes@ferromec.pt", "ricardo.monteiro@ferromec.pt",
                        "Ricardo Monteiro", "+351918223344", "Diretor de Operacoes"),
                new ManagerSeed("contato@novalink-automation.com", "joana.almeida@novalink-automation.com",
                        "Joana Almeida", "+351932221100", "Operations Director"),
                new ManagerSeed("hr@iberiapower.com", "carlos.ruiz@iberiapower.com",
                        "Carlos Ruiz", "+34 618445566", "Maintenance Programs Manager"),
                new ManagerSeed("talent@atlantic-dynamics.eu", "nuno.campos@atlantic-dynamics.eu",
                        "Nuno Campos", "+351937884422", "Chief People Officer"),
                new ManagerSeed("talent@atlantic-robotics.eu", "marta.dominguez@atlantic-robotics.eu",
                        "Marta Dominguez", "+351927556677", "Talent Lead Robotics"),
                new ManagerSeed("contato@lusanaval.pt", "ines.pinto@lusanaval.pt",
                        "Ines Pinto", "+351918770112", "Diretora de Operacoes Offshore"),
                new ManagerSeed("credenciais@terrasol.com", "david.mendes@terrasol.com",
                        "David Mendes", "+351966441122", "Renewables Program Lead"),
                new ManagerSeed("contato@quantumlog.pt", "sofia.rodrigues@quantumlog.pt",
                        "Sofia Rodrigues", "+351933210998", "Automation Logistics Manager"),
                new ManagerSeed("info@polartech-offshore.com", "miguel.barros@polartech-offshore.com",
                        "Miguel Barros", "+351917665500", "Offshore Services Director")
        );
    }

    private record ManagerSeed(
            String companyEmail,
            String email,
            String name,
            String phone,
            String position
    ) { }
}
