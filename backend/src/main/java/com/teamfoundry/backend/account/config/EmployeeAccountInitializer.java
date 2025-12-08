package com.teamfoundry.backend.account.config;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account_options.model.employee.Competence;
import com.teamfoundry.backend.account_options.model.employee.EmployeeCompetence;
import com.teamfoundry.backend.account_options.model.employee.EmployeeFunction;
import com.teamfoundry.backend.account_options.model.employee.EmployeeGeoArea;
import com.teamfoundry.backend.account_options.model.employee.Function;
import com.teamfoundry.backend.account_options.model.employee.GeoArea;
import com.teamfoundry.backend.account_options.repository.employee.CompetenceRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeCompetenceRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeFunctionRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account_options.repository.employee.FunctionRepository;
import com.teamfoundry.backend.account_options.repository.employee.GeoAreaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class EmployeeAccountInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeAccountInitializer.class);

    private final PasswordEncoder passwordEncoder;
    private final FunctionRepository functionRepository;
    private final CompetenceRepository competenceRepository;
    private final GeoAreaRepository geoAreaRepository;
    private final EmployeeFunctionRepository employeeFunctionRepository;
    private final EmployeeCompetenceRepository employeeCompetenceRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;

    @Bean
    @Order(2)
    CommandLineRunner seedEmployees(AccountRepository accountRepository) {
        return args -> {
            // Normaliza contas existentes
            try {
                var all = accountRepository.findAll();
                boolean changed = false;
                for (var acc : all) {
                    boolean updated = false;
                    if (!acc.isVerified()) { acc.setVerified(true); updated = true; }
                    if (acc.getRegistrationStatus() != RegistrationStatus.COMPLETED) { acc.setRegistrationStatus(RegistrationStatus.COMPLETED); updated = true; }
                    changed = changed || updated;
                }
                if (changed) {
                    accountRepository.saveAll(all);
                    LOGGER.info("Updated existing accounts to verified=true and registrationStatus=COMPLETED where needed.");
                }
            } catch (Exception e) {
                LOGGER.warn("Could not normalize existing accounts defaults: {}", e.getMessage());
            }
            if (accountRepository.countByRole(UserType.EMPLOYEE) > 0) {
                LOGGER.debug("Employee accounts already present; skipping seed.");
                return;
            }

            Map<String, Function> functionsByName = loadFunctions(functionRepository);
            Map<String, Competence> competencesByName = loadCompetences(competenceRepository);
            Map<String, GeoArea> geoAreasByName = loadGeoAreas(geoAreaRepository);

            List<EmployeeSeed> seeds = List.of(
                    new EmployeeSeed("joao.silva@teamfoundry.com", 201234567, "Joao", "Silva",
                            "+351912345678", "Portugal", "MALE", LocalDate.of(1990, 5, 21), "password123",
                            List.of("Eletricista"), List.of("Eletricista", "Técnico de AVAC"), List.of("Lisboa", "Porto")),
                    new EmployeeSeed("maria.sousa@teamfoundry.com", 209876543, "Maria", "Sousa",
                            "+351932222333", "Portugal", "FEMALE", LocalDate.of(1992, 3, 17), "password123",
                            List.of("Canalizador"), List.of("Canalizador"), List.of("Braga")),
                    new EmployeeSeed("carlos.oliveira@teamfoundry.com", 301234567, "Carlos", "Oliveira",
                            "+351915667788", "Portugal", "MALE", LocalDate.of(1988, 11, 2), "password123",
                            List.of("Soldador"), List.of("Soldador", "Pintor"), List.of("Faro")),
                    new EmployeeSeed("ana.martins@teamfoundry.com", 309876543, "Ana", "Martins",
                            "+351934556677", "Portugal", "FEMALE", LocalDate.of(1995, 7, 8), "password123",
                            List.of("Carpinteiro"), List.of("Pintor"), List.of("Madeira")),
                    new EmployeeSeed("ricardo.pires@teamfoundry.com", 401234567, "Ricardo", "Pires",
                            "+351918889900", "Portugal", "MALE", LocalDate.of(1993, 1, 29), "password123",
                            List.of("Pedreiro"), List.of("Eletricista"), List.of("Açores")),

                    // extras
                    new EmployeeSeed("sofia.lima@teamfoundry.com", 501234567, "Sofia", "Lima",
                            "+351910000111", "Portugal", "FEMALE", LocalDate.of(1994, 6, 12), "password123",
                            List.of("Eletricista"), List.of("Eletricista"), List.of("Lisboa")),
                    new EmployeeSeed("tiago.rocha@teamfoundry.com", 509876543, "Tiago", "Rocha",
                            "+351910000222", "Portugal", "MALE", LocalDate.of(1991, 9, 5), "password123",
                            List.of("Soldador"), List.of("Soldador", "Técnico de AVAC"), List.of("Porto")),
                    new EmployeeSeed("carla.ferreira@teamfoundry.com", 601234567, "Carla", "Ferreira",
                            "+351910000333", "Portugal", "FEMALE", LocalDate.of(1996, 2, 14), "password123",
                            List.of("Canalizador"), List.of("Canalizador", "Pintor"), List.of("Braga", "Porto")),
                    new EmployeeSeed("miguel.santos@teamfoundry.com", 609876543, "Miguel", "Santos",
                            "+351910000444", "Portugal", "MALE", LocalDate.of(1987, 12, 3), "password123",
                            List.of("Pedreiro"), List.of("Pintor"), List.of("Faro")),
                    new EmployeeSeed("ines.costa@teamfoundry.com", 701234567, "Ines", "Costa",
                            "+351910000555", "Portugal", "FEMALE", LocalDate.of(1998, 4, 27), "password123",
                            List.of("Carpinteiro"), List.of("Canalizador"), List.of("Madeira")),
                    new EmployeeSeed("rui.gomes@teamfoundry.com", 709876543, "Rui", "Gomes",
                            "+351910000666", "Portugal", "MALE", LocalDate.of(1989, 8, 19), "password123",
                            List.of("Eletricista"), List.of("Eletricista", "Soldador"), List.of("Açores", "Lisboa")),
                    new EmployeeSeed("patricia.medeiros@teamfoundry.com", 801234567, "Patricia", "Medeiros",
                            "+351910000777", "Portugal", "FEMALE", LocalDate.of(1993, 10, 30), "password123",
                            List.of("Soldador"), List.of("Soldador"), List.of("Porto", "Braga")),
                    new EmployeeSeed("andre.almeida@teamfoundry.com", 809876543, "Andre", "Almeida",
                            "+351910000888", "Portugal", "MALE", LocalDate.of(1990, 1, 10), "password123",
                            List.of("Pedreiro"), List.of("Eletricista", "Pintor"), List.of("Lisboa")),
                    new EmployeeSeed("luis.figueiredo@teamfoundry.com", 901234567, "Luis", "Figueiredo",
                            "+351910000999", "Portugal", "MALE", LocalDate.of(1986, 7, 22), "password123",
                            List.of("Carpinteiro"), List.of("Soldador"), List.of("Porto", "Faro")),
                    new EmployeeSeed("marta.ribeiro@teamfoundry.com", 909876543, "Marta", "Ribeiro",
                            "+351910001000", "Portugal", "FEMALE", LocalDate.of(1997, 11, 9), "password123",
                            List.of("Canalizador"), List.of("Técnico de AVAC"), List.of("Braga", "Açores")),
                    new EmployeeSeed("daniel.matos@teamfoundry.com", 910111213, "Daniel", "Matos",
                            "+351910001111", "Portugal", "MALE", LocalDate.of(1992, 5, 3), "password123",
                            List.of("Eletricista"), List.of("Eletricista", "Técnico de AVAC"), List.of("Faro", "Lisboa")),
                    new EmployeeSeed("helena.marques@teamfoundry.com", 920111213, "Helena", "Marques",
                            "+351910001222", "Portugal", "FEMALE", LocalDate.of(1994, 2, 18), "password123",
                            List.of("Soldador"), List.of("Soldador", "Pintor"), List.of("Lisboa", "Porto")),
                    new EmployeeSeed("gabriel.fernandes@teamfoundry.com", 930111213, "Gabriel", "Fernandes",
                            "+351910001333", "Portugal", "MALE", LocalDate.of(1991, 6, 9), "password123",
                            List.of("Eletricista"), List.of("Eletricista"), List.of("Porto", "Açores")),
                    new EmployeeSeed("joana.pereira@teamfoundry.com", 940111213, "Joana", "Pereira",
                            "+351910001444", "Portugal", "FEMALE", LocalDate.of(1995, 9, 1), "password123",
                            List.of("Carpinteiro"), List.of("Canalizador", "Pintor"), List.of("Braga", "Lisboa"))
            );

            List<EmployeeFunction> functionRelations = new ArrayList<>();
            List<EmployeeCompetence> competenceRelations = new ArrayList<>();
            List<EmployeeGeoArea> geoAreaRelations = new ArrayList<>();

            seeds.forEach(seed -> {
                EmployeeAccount employee = new EmployeeAccount();
                employee.setEmail(seed.email());
                employee.setNif(seed.nif());
                employee.setPassword(passwordEncoder.encode(seed.rawPassword()));
                employee.setRole(UserType.EMPLOYEE);
                employee.setName(seed.firstName());
                employee.setSurname(seed.lastName());
                employee.setPhone(seed.phone());
                employee.setNationality(seed.nationality());
                employee.setGender(seed.gender());
                employee.setBirthDate(seed.birthDate());
                employee.setVerified(true);
                employee.setRegistrationStatus(RegistrationStatus.COMPLETED);

                EmployeeAccount savedEmployee = accountRepository.save(employee);
                LOGGER.info("Seeded employee account {}.", seed.email());

                seed.functions().forEach(functionName -> {
                    Function function = functionsByName.get(functionName);
                    if (function == null) {
                        LOGGER.warn("Function {} not found; skipping relation for {}.", functionName, seed.email());
                        return;
                    }
                    EmployeeFunction relation = new EmployeeFunction();
                    relation.setEmployee(savedEmployee);
                    relation.setFunction(function);
                    functionRelations.add(relation);
                });

                seed.competences().forEach(competenceName -> {
                    Competence competence = competencesByName.get(competenceName);
                    if (competence == null) {
                        LOGGER.warn("Competence {} not found; skipping relation for {}.", competenceName, seed.email());
                        return;
                    }
                    EmployeeCompetence relation = new EmployeeCompetence();
                    relation.setEmployee(savedEmployee);
                    relation.setCompetence(competence);
                    competenceRelations.add(relation);
                });

                seed.geoAreas().forEach(areaName -> {
                    GeoArea geoArea = geoAreasByName.get(areaName);
                    if (geoArea == null) {
                        LOGGER.warn("Geographic area {} not found; skipping relation for {}.", areaName, seed.email());
                        return;
                    }
                    EmployeeGeoArea relation = new EmployeeGeoArea();
                    relation.setEmployee(savedEmployee);
                    relation.setGeoArea(geoArea);
                    geoAreaRelations.add(relation);
                });
            });

            if (!functionRelations.isEmpty()) {
                employeeFunctionRepository.saveAll(functionRelations);
                LOGGER.info("Seeded {} employee-function relations.", functionRelations.size());
            }
            if (!competenceRelations.isEmpty()) {
                employeeCompetenceRepository.saveAll(competenceRelations);
                LOGGER.info("Seeded {} employee-competence relations.", competenceRelations.size());
            }
            if (!geoAreaRelations.isEmpty()) {
                employeeGeoAreaRepository.saveAll(geoAreaRelations);
                LOGGER.info("Seeded {} employee-geographic area relations.", geoAreaRelations.size());
            }
        };
    }

    private record EmployeeSeed(String email,
                                int nif,
                                String firstName,
                                String lastName,
                                String phone,
                                String nationality,
                                String gender,
                                LocalDate birthDate,
                                String rawPassword,
                                List<String> functions,
                                List<String> competences,
                                List<String> geoAreas) {
    }

    private Map<String, Function> loadFunctions(FunctionRepository repository) {
        Map<String, Function> functions = new HashMap<>();
        repository.findAll().forEach(function -> functions.put(function.getName(), function));
        return functions;
    }

    private Map<String, Competence> loadCompetences(CompetenceRepository repository) {
        Map<String, Competence> competences = new HashMap<>();
        repository.findAll().forEach(competence -> competences.put(competence.getName(), competence));
        return competences;
    }

    private Map<String, GeoArea> loadGeoAreas(GeoAreaRepository repository) {
        Map<String, GeoArea> geoAreas = new HashMap<>();
        repository.findAll().forEach(area -> geoAreas.put(area.getName(), area));
        return geoAreas;
    }
}
