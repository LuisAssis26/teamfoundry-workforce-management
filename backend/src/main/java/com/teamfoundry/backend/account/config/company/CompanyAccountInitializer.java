package com.teamfoundry.backend.account.config.company;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import com.teamfoundry.backend.account.model.company.CompanyActivitySectors;
import com.teamfoundry.backend.account.repository.preferences.PrefActivitySectorsRepository;
import com.teamfoundry.backend.account.repository.company.CompanyActivitySectorsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Profile("!test")
public class CompanyAccountInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyAccountInitializer.class);

    @Bean
    @Order(3)
    CommandLineRunner seedCompanyAccounts(AccountRepository accountRepository,
                                          PasswordEncoder passwordEncoder,
                                          PrefActivitySectorsRepository prefActivitySectorsRepository,
                                          CompanyActivitySectorsRepository companyActivitySectorsRepository) {
        return args -> {
            normalizeExistingAccounts(accountRepository);

            Map<String, PrefActivitySectors> sectorsByName = loadActivitySectors(prefActivitySectorsRepository);
            for (CompanySeed seed : defaultCompanySeeds()) {
                if (accountRepository.existsByEmail(seed.email())) {
                    LOGGER.debug("Company account {} already exists; skipping.", seed.email());
                    continue;
                }

                CompanyAccount company = new CompanyAccount();
                company.setEmail(seed.email());
                company.setNif(seed.nif());
                company.setPassword(passwordEncoder.encode(seed.rawPassword()));
                company.setRole(UserType.COMPANY);
                company.setName(seed.name());
                company.setAddress(seed.address());
                company.setCountry(seed.country());
                company.setPhone(seed.phone());
                company.setWebsite(seed.website());
                company.setDescription(seed.description());
                company.setStatus(seed.status());
                company.setVerified(true);
                company.setRegistrationStatus(RegistrationStatus.COMPLETED);

                CompanyAccount saved = accountRepository.save(company);
                LOGGER.info("Seeded company {}.", saved.getEmail());

                List<CompanyActivitySectors> relations = buildSectorRelations(saved, seed.defaultSectors(), sectorsByName);
                if (!relations.isEmpty()) {
                    companyActivitySectorsRepository.saveAll(relations);
                    LOGGER.info("Seeded {} sector relations for {}.", relations.size(), saved.getEmail());
                }
            }
        };
    }

    private void normalizeExistingAccounts(AccountRepository accountRepository) {
        try {
            var all = accountRepository.findAll();
            boolean changed = false;
            for (Account acc : all) {
                boolean updated = false;
                if (!acc.isVerified()) { acc.setVerified(true); updated = true; }
                if (acc.getRegistrationStatus() != RegistrationStatus.COMPLETED) {
                    acc.setRegistrationStatus(RegistrationStatus.COMPLETED);
                    updated = true;
                }
                changed = changed || updated;
            }
            if (changed) {
                accountRepository.saveAll(all);
                LOGGER.info("Normalized existing accounts to active/completed.");
            }
        } catch (Exception e) {
            LOGGER.warn("Could not normalize accounts: {}", e.getMessage());
        }
    }

    private List<CompanyActivitySectors> buildSectorRelations(CompanyAccount company,
                                                              List<String> sectorNames,
                                                              Map<String, PrefActivitySectors> sectorsByName) {
        List<CompanyActivitySectors> relations = new ArrayList<>();
        for (String sectorName : sectorNames) {
            PrefActivitySectors sector = sectorsByName.get(sectorName);
            if (sector == null) {
                LOGGER.warn("Activity sector {} not found; skipping relation for {}.", sectorName, company.getEmail());
                continue;
            }
            CompanyActivitySectors relation = new CompanyActivitySectors();
            relation.setCompany(company);
            relation.setSector(sector);
            relations.add(relation);
        }
        return relations;
    }

    private Map<String, PrefActivitySectors> loadActivitySectors(PrefActivitySectorsRepository repository) {
        Map<String, PrefActivitySectors> sectors = new HashMap<>();
        repository.findAll().forEach(sector -> sectors.put(sector.getName(), sector));
        return sectors;
    }

    private List<CompanySeed> defaultCompanySeeds() {
        return List.of(
                new CompanySeed(
                        "contact@blueorbitlabs.com",
                        509876321,
                        "password123",
                        "Blue Orbit Labs",
                        "Av. da Liberdade 100, Lisbon, Portugal",
                        "Portugal",
                        "+351213000000",
                        "https://www.blueorbitlabs.com",
                        "Growth-stage HR analytics platform providing workforce insights.",
                        true,
                        List.of("Fundicao", "Manutencao Industrial")
                ),
                new CompanySeed(
                        "operacoes@ferromec.pt",
                        508112233,
                        "password123",
                        "FerroMec Solutions",
                        "Rua das Oficinas 45, Porto, Portugal",
                        "Portugal",
                        "+351221998877",
                        "https://www.ferromec.pt",
                        "Integrador especializado em retrofit e modernizacao de linhas fabris.",
                        true,
                        List.of("Metalomecanica", "Automacao")
                ),
                new CompanySeed(
                        "contato@novalink-automation.com",
                        514882216,
                        "password123",
                        "NovaLink Automation",
                        "Parque Empreendedor 210, Aveiro, Portugal",
                        "Portugal",
                        "+351234330110",
                        "https://www.novalink-automation.com",
                        "Fornecedor de linhas modulares para montagem e inspeção 100% automatizada.",
                        true,
                        List.of("Automacao", "Eletronica")
                ),
                new CompanySeed(
                        "hr@iberiapower.com",
                        504778899,
                        "password123",
                        "Iberia Power Systems",
                        "Av. Energia 250, Madrid, Espanha",
                        "Espanha",
                        "+34 915550000",
                        "https://www.iberiapower.com",
                        "Operador energetico com foco em manutencao e upgrades de subestacoes.",
                        true,
                        List.of("Energia", "Infraestruturas")
                ),
                new CompanySeed(
                        "talent@atlantic-dynamics.eu",
                        523198776,
                        "password123",
                        "Atlantic Dynamics",
                        "Via Atlântica 901, Faro, Portugal",
                        "Portugal",
                        "+351289776554",
                        "https://www.atlantic-dynamics.eu",
                        "Fabricante de manipuladores de alta carga para industria pesada.",
                        true,
                        List.of("Metalomecanica", "Logistica")
                ),
                new CompanySeed(
                        "talent@atlantic-robotics.eu",
                        518654987,
                        "password123",
                        "Atlantic Robotics",
                        "Parque Tecnologico 12, Braga, Portugal",
                        "Portugal",
                        "+351253445566",
                        "https://www.atlantic-robotics.eu",
                        "Desenvolvimento de celulas colaborativas e sistemas de picking automatizado.",
                        false,
                        List.of("Robotica", "Logistica")
                ),
                new CompanySeed(
                        "contato@lusanaval.pt",
                        507441122,
                        "password123",
                        "Lusa Naval Services",
                        "Docas de Leixoes, Matosinhos, Portugal",
                        "Portugal",
                        "+351229880770",
                        "https://www.lusanaval.pt",
                        "Servicos de manutencao offshore e retrofit de embarcacoes.",
                        false,
                        List.of("Fabricacao Pesada", "Manutencao Industrial")
                ),
                new CompanySeed(
                        "credenciais@terrasol.com",
                        516221887,
                        "password123",
                        "TerraSol Renewables",
                        "Estrada das Serras 500, Evora, Portugal",
                        "Portugal",
                        "+351268882210",
                        "https://www.terrasol.com",
                        "Instalacao e manutencao de parques solares de grande escala.",
                        false,
                        List.of("Energia", "Infraestruturas")
                ),
                new CompanySeed(
                        "contato@quantumlog.pt",
                        512903411,
                        "password123",
                        "Quantum Logistics",
                        "Centro Logistico 8, Vila Nova de Gaia, Portugal",
                        "Portugal",
                        "+351223884455",
                        "https://www.quantumlog.pt",
                        "Operador logistico com foco em armazens autonomos e AGVs.",
                        false,
                        List.of("Logistica", "Automacao")
                ),
                new CompanySeed(
                        "info@polartech-offshore.com",
                        525664320,
                        "polartech123",
                        "PolarTech Offshore",
                        "Terminal 4, Viana do Castelo, Portugal",
                        "Portugal",
                        "+351258887700",
                        "https://www.polartech-offshore.com",
                        "Servicos de manutencao e inspeção subaquatica para eolica offshore.",
                        false,
                        List.of("Energia", "Fabricacao Pesada")
                )
        );
    }

    private record CompanySeed(
            String email,
            int nif,
            String rawPassword,
            String name,
            String address,
            String country,
            String phone,
            String website,
            String description,
            boolean status,
            List<String> defaultSectors
    ) { }
}
