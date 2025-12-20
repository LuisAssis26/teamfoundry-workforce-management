package com.teamfoundry.backend.account.config.company;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.company.CompanyActivitySectors;
import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyActivitySectorsRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefActivitySectorsRepository;
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
                CompanyAccount company = null;
                var existing = accountRepository.findByEmail(seed.email());
                if (existing.isPresent()) {
                    if (existing.get() instanceof CompanyAccount) {
                        company = (CompanyAccount) existing.get();
                        LOGGER.debug("Company account {} already exists; skipping create.", seed.email());
                    } else {
                        LOGGER.warn("Account {} exists but is not a company; skipping company seed.", seed.email());
                        continue;
                    }
                } else {
                    company = new CompanyAccount();
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

                    company = accountRepository.save(company);
                    LOGGER.info("Seeded company {}.", company.getEmail());
                }

                List<CompanyActivitySectors> relations = buildSectorRelations(company, seed.defaultSectors(), sectorsByName);
                if (!relations.isEmpty()) {
                    var existingRelations = companyActivitySectorsRepository.findByCompany(company);
                    var existingNames = existingRelations.stream()
                            .map(relation -> relation.getSector().getName().toLowerCase())
                            .collect(java.util.stream.Collectors.toSet());
                    List<CompanyActivitySectors> toPersist = new ArrayList<>();
                    for (CompanyActivitySectors relation : relations) {
                        String sectorName = relation.getSector().getName().toLowerCase();
                        if (existingNames.contains(sectorName)) {
                            continue;
                        }
                        toPersist.add(relation);
                    }
                    if (!toPersist.isEmpty()) {
                        companyActivitySectorsRepository.saveAll(toPersist);
                        LOGGER.info("Seeded {} sector relations for {}.", toPersist.size(), company.getEmail());
                    }
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
                new CompanySeed("contact@blueorbitlabs.com", 509876321, "password123", "Blue Orbit Labs",
                        "Av. da Liberdade 100, Lisbon, Portugal", "Portugal", "+351213000000",
                        "https://www.blueorbitlabs.com",
                        "Growth-stage HR analytics platform providing workforce insights.",
                        true, List.of("Fundicao", "Manutencao Industrial")),
                new CompanySeed("operacoes@ferromec.pt", 508112233, "password123", "FerroMec Solutions",
                        "Rua das Oficinas 45, Porto, Portugal", "Portugal", "+351221998877",
                        "https://www.ferromec.pt",
                        "Integrador especializado em retrofit e modernizacao de linhas fabris.",
                        true, List.of("Metalomecanica", "Automacao")),
                new CompanySeed("contato@novalink-automation.com", 514882216, "password123", "NovaLink Automation",
                        "Parque Empreendedor 210, Aveiro, Portugal", "Portugal", "+351234330110",
                        "https://www.novalink-automation.com",
                        "Fornecedor de linhas modulares para montagem e inspecao 100% automatizada.",
                        true, List.of("Automacao", "Eletronica")),
                new CompanySeed("hr@iberiapower.com", 504778899, "password123", "Iberia Power Systems",
                        "Av. Energia 250, Madrid, Espanha", "Espanha", "+34 915550000",
                        "https://www.iberiapower.com",
                        "Operador energetico com foco em manutencao e upgrades de subestacoes.",
                        true, List.of("Energia", "Infraestruturas")),
                new CompanySeed("talent@atlantic-dynamics.eu", 523198776, "password123", "Atlantic Dynamics",
                        "Via Atlantica 901, Faro, Portugal", "Portugal", "+351289776554",
                        "https://www.atlantic-dynamics.eu",
                        "Fabricante de manipuladores de alta carga para industria pesada.",
                        true, List.of("Metalomecanica", "Logistica")),
                new CompanySeed("talent@atlantic-robotics.eu", 518654987, "password123", "Atlantic Robotics",
                        "Parque Tecnologico 12, Braga, Portugal", "Portugal", "+351253445566",
                        "https://www.atlantic-robotics.eu",
                        "Desenvolvimento de celulas colaborativas e sistemas de picking automatizado.",
                        false, List.of("Robotica", "Logistica")),
                new CompanySeed("contato@lusanaval.pt", 507441122, "password123", "Lusa Naval Services",
                        "Docas de Leixoes, Matosinhos, Portugal", "Portugal", "+351229880770",
                        "https://www.lusanaval.pt",
                        "Servicos de manutencao offshore e retrofit de embarcacoes.",
                        false, List.of("Fabricacao Pesada", "Manutencao Industrial")),
                new CompanySeed("credenciais@terrasol.com", 516221887, "password123", "TerraSol Renewables",
                        "Estrada das Serras 500, Evora, Portugal", "Portugal", "+351268882210",
                        "https://www.terrasol.com",
                        "Instalacao e manutencao de parques solares de grande escala.",
                        false, List.of("Energia", "Infraestruturas")),
                new CompanySeed("contato@quantumlog.pt", 512903411, "password123", "Quantum Logistics",
                        "Centro Logistico 8, Vila Nova de Gaia, Portugal", "Portugal", "+351223884455",
                        "https://www.quantumlog.pt",
                        "Operador logistico com foco em armazens autonomos e AGVs.",
                        false, List.of("Logistica", "Automacao")),
                new CompanySeed("info@polartech-offshore.com", 525664320, "password123", "PolarTech Offshore",
                        "Terminal 4, Viana do Castelo, Portugal", "Portugal", "+351258887700",
                        "https://www.polartech-offshore.com",
                        "Servicos de manutencao e inspecao subaquatica para eolica offshore.",
                        false, List.of("Energia", "Fabricacao Pesada")),
                new CompanySeed("contact@axiomechanics.com", 526331144, "password123", "Axio Mechanics",
                        "Rua das Engenharias 12, Porto, Portugal", "Portugal", "+351221555444",
                        "https://www.axiomechanics.com",
                        "Consultoria em integridade estrutural e testes nao destrutivos.",
                        true, List.of("Metalomecanica", "Fabricacao Pesada")),
                new CompanySeed("talent@neovolt.pt", 527441155, "password123", "NeoVolt Systems",
                        "Campus Energia 88, Sines, Portugal", "Portugal", "+351269887744",
                        "https://www.neovolt.pt",
                        "Integrador de sistemas de armazenamento e conversao de energia.",
                        true, List.of("Energia", "Automacao")),
                new CompanySeed("careers@deltaproc.com", 528771166, "password123", "DeltaProc Logistics",
                        "Parque Industrial 45, Setubal, Portugal", "Portugal", "+351265778899",
                        "https://www.deltaproc.com",
                        "Operador logistico especializado em cadeia fria e rastreabilidade.",
                        true, List.of("Logistica", "Infraestruturas")),
                new CompanySeed("hr@silverforge.eu", 529882177, "password123", "SilverForge",
                        "Zona Industrial 23, Guimaraes, Portugal", "Portugal", "+351253889900",
                        "https://www.silverforge.eu",
                        "Fundicao de precisao e tratamentos termicos para series curtas.",
                        false, List.of("Fundicao", "Manutencao Industrial")),
                new CompanySeed("contato@smartlift-pt.com", 530991188, "password123", "SmartLift Elevacao",
                        "Av. do Trabalho 300, Coimbra, Portugal", "Portugal", "+351239778800",
                        "https://www.smartlift-pt.com",
                        "Projetos e manutencao de sistemas de elevacao industrial.",
                        false, List.of("Infraestruturas", "Automacao")),
                new CompanySeed("jobs@oceantec.pt", 531002199, "password123", "OceanTec Services",
                        "Molhe Maritimo 5, Aveiro, Portugal", "Portugal", "+351234770011",
                        "https://www.oceantec.pt",
                        "Inspecao e reparacao de ativos costeiros e offshore.",
                        false, List.of("Fabricacao Pesada", "Energia")),
                new CompanySeed("gente@andrade-robotics.com", 532113200, "password123", "Andrade Robotics",
                        "Parque Tecnologico 99, Braga, Portugal", "Portugal", "+351253889901",
                        "https://www.andrade-robotics.com",
                        "Celulas roboticas customizadas para pick-and-place e soldadura.",
                        false, List.of("Robotica", "Automacao")),
                new CompanySeed("talentos@flexipack.eu", 533224211, "password123", "FlexiPack Europe",
                        "Zona Industrial Embalagens 14, Viseu, Portugal", "Portugal", "+351232889922",
                        "https://www.flexipack.eu",
                        "Linhas de embalagem e paletizacao flexiveis para FMCG.",
                        false, List.of("Logistica", "Metalomecanica")),
                new CompanySeed("jobs@auroragrid.com", 534335222, "password123", "Aurora Grid",
                        "Av. das Redes 77, Lisboa, Portugal", "Portugal", "+351210778833",
                        "https://www.auroragrid.com",
                        "Modernizacao de redes de distribuicao com IoT e automacao.",
                        false, List.of("Energia", "Infraestruturas")),
                new CompanySeed("contato@fulgentmachines.com", 535446233, "password123", "Fulgent Machines",
                        "Polo Industrial 55, Setubal, Portugal", "Portugal", "+351265880990",
                        "https://www.fulgentmachines.com",
                        "Fabricacao de linhas especiais de montagem e teste.",
                        false, List.of("Metalomecanica", "Automacao"))
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
