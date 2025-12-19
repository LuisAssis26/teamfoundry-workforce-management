package com.teamfoundry.backend.account.config.employee;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import com.teamfoundry.backend.account.model.preferences.PrefRole;
import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeRole;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea;
import com.teamfoundry.backend.account.repository.preferences.PrefSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefRoleRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefGeoAreaRepository;
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
    private final PrefRoleRepository prefRoleRepository;
    private final PrefSkillRepository prefSkillRepository;
    private final PrefGeoAreaRepository prefGeoAreaRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
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

            Map<String, PrefRole> functionsByName = loadFunctions(prefRoleRepository);
            Map<String, PrefSkill> competencesByName = loadCompetences(prefSkillRepository);
            Map<String, PrefGeoArea> geoAreasByName = loadGeoAreas(prefGeoAreaRepository);

                        List<EmployeeSeed> seeds = List.of(
                    new EmployeeSeed("joao.silva@teamfoundry.com", 201234567, "Joao", "Silva",
                            "+351912345678", "Portugal", "MALE", LocalDate.of(1990, 5, 21), "password123",
                            List.of("Eletricista"), List.of("Lideranca", "Comunicacao eficaz"), List.of("Lisboa", "Porto")),
                    new EmployeeSeed("maria.sousa@teamfoundry.com", 209876543, "Maria", "Sousa",
                            "+351932222333", "Portugal", "FEMALE", LocalDate.of(1992, 3, 17), "password123",
                            List.of("Canalizador"), List.of("Resolucao de problemas", "Adaptabilidade"), List.of("Braga")),
                    new EmployeeSeed("carlos.oliveira@teamfoundry.com", 301234567, "Carlos", "Oliveira",
                            "+351915667788", "Portugal", "MALE", LocalDate.of(1988, 11, 2), "password123",
                            List.of("Soldador"), List.of("Pensamento critico", "Gestao de tempo"), List.of("Faro")),
                    new EmployeeSeed("ana.martins@teamfoundry.com", 309876543, "Ana", "Martins",
                            "+351934556677", "Portugal", "FEMALE", LocalDate.of(1995, 7, 8), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Atencao aos detalhes"), List.of("Madeira")),
                    new EmployeeSeed("ricardo.pires@teamfoundry.com", 401234567, "Ricardo", "Pires",
                            "+351918889900", "Portugal", "MALE", LocalDate.of(1993, 1, 29), "password123",
                            List.of("Pedreiro"), List.of("Proatividade", "Orientacao para resultados"), List.of("Lisboa")),

                    // Extras
                    new EmployeeSeed("sofia.lima@teamfoundry.com", 501234567, "Sofia", "Lima",
                            "+351910000111", "Portugal", "FEMALE", LocalDate.of(1994, 6, 12), "password123",
                            List.of("Eletricista"), List.of("Comunicacao eficaz", "Trabalho em equipa"), List.of("Lisboa")),
                    new EmployeeSeed("tiago.rocha@teamfoundry.com", 509876543, "Tiago", "Rocha",
                            "+351910000222", "Portugal", "MALE", LocalDate.of(1991, 9, 5), "password123",
                            List.of("Soldador"), List.of("Gestao de conflitos", "Pensamento critico"), List.of("Porto")),
                    new EmployeeSeed("carla.ferreira@teamfoundry.com", 601234567, "Carla", "Ferreira",
                            "+351910000333", "Portugal", "FEMALE", LocalDate.of(1996, 2, 14), "password123",
                            List.of("Canalizador"), List.of("Gestao de tempo", "Adaptabilidade"), List.of("Braga", "Porto")),
                    new EmployeeSeed("miguel.santos@teamfoundry.com", 609876543, "Miguel", "Santos",
                            "+351910000444", "Portugal", "MALE", LocalDate.of(1987, 12, 3), "password123",
                            List.of("Pedreiro"), List.of("Resolucao de problemas", "Tomada de decisao"), List.of("Faro")),
                    new EmployeeSeed("ines.costa@teamfoundry.com", 701234567, "Ines", "Costa",
                            "+351910000555", "Portugal", "FEMALE", LocalDate.of(1998, 4, 27), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Organizacao"), List.of("Madeira")),
                    new EmployeeSeed("rui.gomes@teamfoundry.com", 709876543, "Rui", "Gomes",
                            "+351910000666", "Portugal", "MALE", LocalDate.of(1989, 8, 19), "password123",
                            List.of("Eletricista"), List.of("Proatividade", "Orientacao para resultados"), List.of("Lisboa")),
                    new EmployeeSeed("patricia.medeiros@teamfoundry.com", 801234567, "Patricia", "Medeiros",
                            "+351910000777", "Portugal", "FEMALE", LocalDate.of(1993, 10, 30), "password123",
                            List.of("Soldador"), List.of("Atencao aos detalhes", "Gestao de tempo"), List.of("Porto", "Braga")),
                    new EmployeeSeed("andre.almeida@teamfoundry.com", 809876543, "Andre", "Almeida",
                            "+351910000888", "Portugal", "MALE", LocalDate.of(1990, 1, 10), "password123",
                            List.of("Pedreiro"), List.of("Lideranca", "Tomada de decisao"), List.of("Lisboa")),
                    new EmployeeSeed("luis.figueiredo@teamfoundry.com", 901234567, "Luis", "Figueiredo",
                            "+351910000999", "Portugal", "MALE", LocalDate.of(1986, 7, 22), "password123",
                            List.of("Carpinteiro"), List.of("Gestao de conflitos", "Trabalho em equipa"), List.of("Porto", "Faro")),
                    new EmployeeSeed("marta.ribeiro@teamfoundry.com", 909876543, "Marta", "Ribeiro",
                            "+351910001000", "Portugal", "FEMALE", LocalDate.of(1997, 11, 9), "password123",
                            List.of("Canalizador"), List.of("Adaptabilidade", "Comunicacao eficaz"), List.of("Braga", "Lisboa")),
                    new EmployeeSeed("daniel.matos@teamfoundry.com", 910111213, "Daniel", "Matos",
                            "+351910001111", "Portugal", "MALE", LocalDate.of(1992, 5, 3), "password123",
                            List.of("Eletricista"), List.of("Aprendizado rapido", "Orientacao para resultados"), List.of("Faro", "Lisboa")),
                    new EmployeeSeed("helena.marques@teamfoundry.com", 920111213, "Helena", "Marques",
                            "+351910001222", "Portugal", "FEMALE", LocalDate.of(1994, 2, 18), "password123",
                            List.of("Soldador"), List.of("Gestao de tempo", "Criatividade"), List.of("Lisboa", "Porto")),
                    new EmployeeSeed("gabriel.fernandes@teamfoundry.com", 930111213, "Gabriel", "Fernandes",
                            "+351910001333", "Portugal", "MALE", LocalDate.of(1991, 6, 9), "password123",
                            List.of("Eletricista"), List.of("Pensamento critico", "Proatividade"), List.of("Porto", "Lisboa")),
                    new EmployeeSeed("joana.pereira@teamfoundry.com", 940111213, "Joana", "Pereira",
                            "+351910001444", "Portugal", "FEMALE", LocalDate.of(1995, 9, 1), "password123",
                            List.of("Carpinteiro"), List.of("Trabalho em equipa", "Gestao de conflitos"), List.of("Braga", "Lisboa")),

                    new EmployeeSeed("paulo.cardoso@teamfoundry.com", 950111213, "Paulo", "Cardoso",
                            "+351910001555", "Portugal", "MALE", LocalDate.of(1987, 3, 15), "password123",
                            List.of("Pedreiro"), List.of("Resolucao de problemas", "Organizacao"), List.of("Porto")),
                    new EmployeeSeed("beatriz.neves@teamfoundry.com", 960111213, "Beatriz", "Neves",
                            "+351910001666", "Portugal", "FEMALE", LocalDate.of(1996, 8, 25), "password123",
                            List.of("Canalizador"), List.of("Aprendizado rapido", "Atencao aos detalhes"), List.of("Lisboa")),
                    new EmployeeSeed("nuno.vieira@teamfoundry.com", 970111213, "Nuno", "Vieira",
                            "+351910001777", "Portugal", "MALE", LocalDate.of(1990, 10, 5), "password123",
                            List.of("Soldador"), List.of("Orientacao para resultados", "Comunicacao eficaz"), List.of("Faro")),
                    new EmployeeSeed("filipa.coelho@teamfoundry.com", 980111213, "Filipa", "Coelho",
                            "+351910001888", "Portugal", "FEMALE", LocalDate.of(1993, 12, 19), "password123",
                            List.of("Eletricista"), List.of("Lideranca", "Gestao de tempo"), List.of("Braga")),
                    new EmployeeSeed("fernando.teixeira@teamfoundry.com", 990111213, "Fernando", "Teixeira",
                            "+351910001999", "Portugal", "MALE", LocalDate.of(1985, 4, 9), "password123",
                            List.of("Carpinteiro"), List.of("Pensamento critico", "Trabalho em equipa"), List.of("Porto")),
                    new EmployeeSeed("sara.barbosa@teamfoundry.com", 100011121, "Sara", "Barbosa",
                            "+351910002000", "Portugal", "FEMALE", LocalDate.of(1997, 6, 30), "password123",
                            List.of("Pedreiro"), List.of("Adaptabilidade", "Tomada de decisao"), List.of("Lisboa")),
                    new EmployeeSeed("diogo.melo@teamfoundry.com", 100211121, "Diogo", "Melo",
                            "+351910002111", "Portugal", "MALE", LocalDate.of(1992, 1, 12), "password123",
                            List.of("Canalizador"), List.of("Gestao de conflitos", "Proatividade"), List.of("Braga")),
                    new EmployeeSeed("catarina.pinto@teamfoundry.com", 100311121, "Catarina", "Pinto",
                            "+351910002222", "Portugal", "FEMALE", LocalDate.of(1994, 9, 23), "password123",
                            List.of("Soldador"), List.of("Criatividade", "Orientacao para resultados"), List.of("Porto")),
                    new EmployeeSeed("bruno.leite@teamfoundry.com", 100411121, "Bruno", "Leite",
                            "+351910002333", "Portugal", "MALE", LocalDate.of(1988, 2, 2), "password123",
                            List.of("Eletricista"), List.of("Resolucao de problemas", "Gestao de tempo"), List.of("Faro")),
                    new EmployeeSeed("vera.cunha@teamfoundry.com", 100511121, "Vera", "Cunha",
                            "+351910002444", "Portugal", "FEMALE", LocalDate.of(1996, 11, 11), "password123",
                            List.of("Carpinteiro"), List.of("Comunicacao eficaz", "Atencao aos detalhes"), List.of("Lisboa")),
                    new EmployeeSeed("pedro.lopes@teamfoundry.com", 100611121, "Pedro", "Lopes",
                            "+351910002555", "Portugal", "MALE", LocalDate.of(1991, 7, 17), "password123",
                            List.of("Pedreiro"), List.of("Lideranca", "Trabalho em equipa"), List.of("Porto")),

                    new EmployeeSeed("eduardo.moraes@teamfoundry.com", 100711121, "Eduardo", "Moraes",
                            "+351910002666", "Portugal", "MALE", LocalDate.of(1989, 3, 14), "password123",
                            List.of("Eletricista"), List.of("Lideranca", "Gestao de tempo"), List.of("Lisboa")),
                    new EmployeeSeed("juliana.alves@teamfoundry.com", 100811121, "Juliana", "Alves",
                            "+351910002777", "Portugal", "FEMALE", LocalDate.of(1995, 5, 20), "password123",
                            List.of("Canalizador"), List.of("Comunicacao eficaz", "Resolucao de problemas"), List.of("Porto")),
                    new EmployeeSeed("sandra.sousa@teamfoundry.com", 100911121, "Sandra", "Sousa",
                            "+351910002888", "Portugal", "FEMALE", LocalDate.of(1993, 4, 6), "password123",
                            List.of("Soldador"), List.of("Pensamento critico", "Trabalho em equipa"), List.of("Braga")),
                    new EmployeeSeed("felipe.carvalho@teamfoundry.com", 101011121, "Felipe", "Carvalho",
                            "+351910002999", "Portugal", "MALE", LocalDate.of(1986, 10, 2), "password123",
                            List.of("Pedreiro"), List.of("Proatividade", "Organizacao"), List.of("Faro")),
                    new EmployeeSeed("renata.dias@teamfoundry.com", 101111121, "Renata", "Dias",
                            "+351910003000", "Portugal", "FEMALE", LocalDate.of(1994, 1, 25), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Atencao aos detalhes"), List.of("Madeira")),
                    new EmployeeSeed("vitor.lima@teamfoundry.com", 101211121, "Vitor", "Lima",
                            "+351910003111", "Portugal", "MALE", LocalDate.of(1990, 9, 18), "password123",
                            List.of("Eletricista"), List.of("Orientacao para resultados", "Gestao de conflitos"), List.of("Porto")),
                    new EmployeeSeed("talita.costa@teamfoundry.com", 101311121, "Talita", "Costa",
                            "+351910003222", "Portugal", "FEMALE", LocalDate.of(1996, 12, 7), "password123",
                            List.of("Soldador"), List.of("Gestao de tempo", "Resolucao de problemas"), List.of("Lisboa")),
                    new EmployeeSeed("mauricio.ramos@teamfoundry.com", 101411121, "Mauricio", "Ramos",
                            "+351910003333", "Portugal", "MALE", LocalDate.of(1988, 8, 28), "password123",
                            List.of("Canalizador"), List.of("Adaptabilidade", "Comunicacao eficaz"), List.of("Braga")),
                    new EmployeeSeed("bruna.silveira@teamfoundry.com", 101511121, "Bruna", "Silveira",
                            "+351910003444", "Portugal", "FEMALE", LocalDate.of(1997, 3, 3), "password123",
                            List.of("Pedreiro"), List.of("Tomada de decisao", "Proatividade"), List.of("Faro")),
                    new EmployeeSeed("marcelo.borges@teamfoundry.com", 101611121, "Marcelo", "Borges",
                            "+351910003555", "Portugal", "MALE", LocalDate.of(1991, 6, 15), "password123",
                            List.of("Carpinteiro"), List.of("Lideranca", "Trabalho em equipa"), List.of("Porto")),
                    new EmployeeSeed("clara.monteiro@teamfoundry.com", 101711121, "Clara", "Monteiro",
                            "+351910003666", "Portugal", "FEMALE", LocalDate.of(1995, 2, 11), "password123",
                            List.of("Eletricista"), List.of("Aprendizado rapido", "Criatividade"), List.of("Lisboa")),
                    new EmployeeSeed("ivo.azevedo@teamfoundry.com", 101811121, "Ivo", "Azevedo",
                            "+351910003777", "Portugal", "MALE", LocalDate.of(1987, 5, 9), "password123",
                            List.of("Soldador"), List.of("Orientacao para resultados", "Gestao de tempo"), List.of("Braga")),
                    new EmployeeSeed("aline.ferraz@teamfoundry.com", 101911121, "Aline", "Ferraz",
                            "+351910003888", "Portugal", "FEMALE", LocalDate.of(1996, 7, 26), "password123",
                            List.of("Canalizador"), List.of("Atencao aos detalhes", "Organizacao"), List.of("Porto")),
                    new EmployeeSeed("sergio.machado@teamfoundry.com", 102011121, "Sergio", "Machado",
                            "+351910003999", "Portugal", "MALE", LocalDate.of(1984, 11, 4), "password123",
                            List.of("Pedreiro"), List.of("Pensamento critico", "Resolucao de problemas"), List.of("Faro")),
                    new EmployeeSeed("monica.teixeira@teamfoundry.com", 102111121, "Monica", "Teixeira",
                            "+351910004000", "Portugal", "FEMALE", LocalDate.of(1993, 10, 13), "password123",
                            List.of("Carpinteiro"), List.of("Proatividade", "Comunicacao eficaz"), List.of("Madeira")),
                    new EmployeeSeed("hugo.barros@teamfoundry.com", 102211121, "Hugo", "Barros",
                            "+351910004111", "Portugal", "MALE", LocalDate.of(1989, 1, 6), "password123",
                            List.of("Eletricista"), List.of("Gestao de conflitos", "Trabalho em equipa"), List.of("Lisboa")),
                    new EmployeeSeed("laura.freitas@teamfoundry.com", 102311121, "Laura", "Freitas",
                            "+351910004222", "Portugal", "FEMALE", LocalDate.of(1997, 8, 30), "password123",
                            List.of("Soldador"), List.of("Adaptabilidade", "Tomada de decisao"), List.of("Porto")),
                    new EmployeeSeed("douglas.cunha@teamfoundry.com", 102411121, "Douglas", "Cunha",
                            "+351910004333", "Portugal", "MALE", LocalDate.of(1990, 4, 21), "password123",
                            List.of("Canalizador"), List.of("Lideranca", "Gestao de tempo"), List.of("Braga")),
                    new EmployeeSeed("simone.pires@teamfoundry.com", 102511121, "Simone", "Pires",
                            "+351910004444", "Portugal", "FEMALE", LocalDate.of(1994, 9, 17), "password123",
                            List.of("Pedreiro"), List.of("Orientacao para resultados", "Atencao aos detalhes"), List.of("Faro")),
                    new EmployeeSeed("nelson.faria@teamfoundry.com", 102611121, "Nelson", "Faria",
                            "+351910004555", "Portugal", "MALE", LocalDate.of(1988, 12, 1), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Proatividade"), List.of("Porto")),
                    new EmployeeSeed("daniela.rosa@teamfoundry.com", 102711121, "Daniela", "Rosa",
                            "+351910004666", "Portugal", "FEMALE", LocalDate.of(1996, 3, 28), "password123",
                            List.of("Eletricista"), List.of("Resolucao de problemas", "Comunicacao eficaz"), List.of("Lisboa")),
                    new EmployeeSeed("rafael.dantas@teamfoundry.com", 102811121, "Rafael", "Dantas",
                            "+351910004777", "Portugal", "MALE", LocalDate.of(1991, 6, 8), "password123",
                            List.of("Soldador"), List.of("Pensamento critico", "Organizacao"), List.of("Braga")),
                    new EmployeeSeed("gisela.lopes@teamfoundry.com", 102911121, "Gisela", "Lopes",
                            "+351910004888", "Portugal", "FEMALE", LocalDate.of(1995, 2, 3), "password123",
                            List.of("Canalizador"), List.of("Trabalho em equipa", "Gestao de tempo"), List.of("Porto")),
                    new EmployeeSeed("arthur.guedes@teamfoundry.com", 103011121, "Arthur", "Guedes",
                            "+351910004999", "Portugal", "MALE", LocalDate.of(1987, 7, 24), "password123",
                            List.of("Pedreiro"), List.of("Gestao de conflitos", "Adaptabilidade"), List.of("Faro")),
                    new EmployeeSeed("bianca.cardoso@teamfoundry.com", 103111121, "Bianca", "Cardoso",
                            "+351910005000", "Portugal", "FEMALE", LocalDate.of(1998, 5, 29), "password123",
                            List.of("Carpinteiro"), List.of("Tomada de decisao", "Orientacao para resultados"), List.of("Madeira")),
                    new EmployeeSeed("caio.moreira@teamfoundry.com", 103211121, "Caio", "Moreira",
                            "+351910005111", "Portugal", "MALE", LocalDate.of(1992, 11, 16), "password123",
                            List.of("Eletricista"), List.of("Aprendizado rapido", "Atencao aos detalhes"), List.of("Braga")),
                    new EmployeeSeed("priscila.nunes@teamfoundry.com", 103311121, "Priscila", "Nunes",
                            "+351910005222", "Portugal", "FEMALE", LocalDate.of(1993, 1, 19), "password123",
                            List.of("Soldador"), List.of("Proatividade", "Criatividade"), List.of("Porto")),
                    new EmployeeSeed("andreia.fonseca@teamfoundry.com", 103411121, "Andreia", "Fonseca",
                            "+351910005333", "Portugal", "FEMALE", LocalDate.of(1995, 9, 4), "password123",
                            List.of("Canalizador"), List.of("Organizacao", "Pensamento critico"), List.of("Lisboa")),
                    new EmployeeSeed("mateus.tavares@teamfoundry.com", 103511121, "Mateus", "Tavares",
                            "+351910005444", "Portugal", "MALE", LocalDate.of(1989, 10, 27), "password123",
                            List.of("Pedreiro"), List.of("Lideranca", "Resolucao de problemas"), List.of("Faro")),
                    new EmployeeSeed("vivian.almeida@teamfoundry.com", 103611121, "Vivian", "Almeida",
                            "+351910005555", "Portugal", "FEMALE", LocalDate.of(1997, 12, 8), "password123",
                            List.of("Carpinteiro"), List.of("Comunicacao eficaz", "Trabalho em equipa"), List.of("Porto")),
                    new EmployeeSeed("gustavo.mendes@teamfoundry.com", 103711121, "Gustavo", "Mendes",
                            "+351910005666", "Portugal", "MALE", LocalDate.of(1990, 2, 10), "password123",
                            List.of("Eletricista"), List.of("Resolucao de problemas", "Gestao de tempo"), List.of("Lisboa", "Braga")),
                    new EmployeeSeed("larissa.gomes@teamfoundry.com", 103811121, "Larissa", "Gomes",
                            "+351910005777", "Portugal", "FEMALE", LocalDate.of(1995, 4, 18), "password123",
                            List.of("Soldador"), List.of("Comunicacao eficaz", "Trabalho em equipa"), List.of("Porto")),
                    new EmployeeSeed("henrique.oliveira@teamfoundry.com", 103911121, "Henrique", "Oliveira",
                            "+351910005888", "Portugal", "MALE", LocalDate.of(1988, 9, 2), "password123",
                            List.of("Canalizador"), List.of("Adaptabilidade", "Organizacao"), List.of("Faro")),
                    new EmployeeSeed("tania.silva@teamfoundry.com", 104011121, "Tania", "Silva",
                            "+351910005999", "Portugal", "FEMALE", LocalDate.of(1992, 11, 21), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Atencao aos detalhes"), List.of("Madeira")),
                    new EmployeeSeed("bruno.simoes@teamfoundry.com", 104111121, "Bruno", "Simoes",
                            "+351910006000", "Portugal", "MALE", LocalDate.of(1987, 6, 14), "password123",
                            List.of("Pedreiro"), List.of("Orientacao para resultados", "Proatividade"), List.of("Lisboa")),
                    new EmployeeSeed("raquel.moura@teamfoundry.com", 104211121, "Raquel", "Moura",
                            "+351910006111", "Portugal", "FEMALE", LocalDate.of(1996, 1, 9), "password123",
                            List.of("Eletricista"), List.of("Aprendizado rapido", "Comunicacao eficaz"), List.of("Porto", "Braga")),
                    new EmployeeSeed("felix.andrade@teamfoundry.com", 104311121, "Felix", "Andrade",
                            "+351910006222", "Portugal", "MALE", LocalDate.of(1989, 3, 27), "password123",
                            List.of("Soldador"), List.of("Gestao de conflitos", "Pensamento critico"), List.of("Faro", "Lisboa")),
                    new EmployeeSeed("julio.ramos@teamfoundry.com", 104411121, "Julio", "Ramos",
                            "+351910006333", "Portugal", "MALE", LocalDate.of(1991, 7, 12), "password123",
                            List.of("Canalizador"), List.of("Resolucao de problemas", "Trabalho em equipa"), List.of("Braga")),
                    new EmployeeSeed("marina.rocha@teamfoundry.com", 104511121, "Marina", "Rocha",
                            "+351910006444", "Portugal", "FEMALE", LocalDate.of(1994, 10, 5), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Gestao de tempo"), List.of("Porto")),
                    new EmployeeSeed("tiago.freire@teamfoundry.com", 104611121, "Tiago", "Freire",
                            "+351910006555", "Portugal", "MALE", LocalDate.of(1986, 8, 19), "password123",
                            List.of("Pedreiro"), List.of("Orientacao para resultados", "Organizacao"), List.of("Lisboa", "Faro")),
                    new EmployeeSeed("lara.matos@teamfoundry.com", 104711121, "Lara", "Matos",
                            "+351910006666", "Portugal", "FEMALE", LocalDate.of(1997, 2, 23), "password123",
                            List.of("Eletricista"), List.of("Atencao aos detalhes", "Aprendizado rapido"), List.of("Braga")),
                    new EmployeeSeed("rodrigo.teixeira@teamfoundry.com", 104811121, "Rodrigo", "Teixeira",
                            "+351910006777", "Portugal", "MALE", LocalDate.of(1985, 12, 30), "password123",
                            List.of("Soldador"), List.of("Gestao de tempo", "Pensamento critico"), List.of("Porto")),
                    new EmployeeSeed("camila.pereira@teamfoundry.com", 104911121, "Camila", "Pereira",
                            "+351910006888", "Portugal", "FEMALE", LocalDate.of(1993, 5, 17), "password123",
                            List.of("Canalizador"), List.of("Comunicacao eficaz", "Adaptabilidade"), List.of("Lisboa")),
                    new EmployeeSeed("leonardo.barbosa@teamfoundry.com", 105011121, "Leonardo", "Barbosa",
                            "+351910006999", "Portugal", "MALE", LocalDate.of(1990, 4, 2), "password123",
                            List.of("Carpinteiro"), List.of("Proatividade", "Atencao aos detalhes"), List.of("Faro")),
                    new EmployeeSeed("rita.carvalho@teamfoundry.com", 105111121, "Rita", "Carvalho",
                            "+351910007000", "Portugal", "FEMALE", LocalDate.of(1995, 6, 8), "password123",
                            List.of("Pedreiro"), List.of("Resolucao de problemas", "Gestao de conflitos"), List.of("Porto", "Lisboa")),
                    new EmployeeSeed("alexandre.pinto@teamfoundry.com", 105211121, "Alexandre", "Pinto",
                            "+351910007111", "Portugal", "MALE", LocalDate.of(1988, 1, 15), "password123",
                            List.of("Eletricista"), List.of("Lideranca", "Trabalho em equipa"), List.of("Braga")),
                    new EmployeeSeed("susana.correia@teamfoundry.com", 105311121, "Susana", "Correia",
                            "+351910007222", "Portugal", "FEMALE", LocalDate.of(1994, 9, 11), "password123",
                            List.of("Soldador"), List.of("Organizacao", "Pensamento critico"), List.of("Lisboa", "Porto")),
                    new EmployeeSeed("mario.lopes@teamfoundry.com", 105411121, "Mario", "Lopes",
                            "+351910007333", "Portugal", "MALE", LocalDate.of(1987, 3, 25), "password123",
                            List.of("Canalizador"), List.of("Proatividade", "Comunicacao eficaz"), List.of("Braga", "Faro")),
                    new EmployeeSeed("paula.faria@teamfoundry.com", 105511121, "Paula", "Faria",
                            "+351910007444", "Portugal", "FEMALE", LocalDate.of(1996, 12, 2), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Trabalho em equipa"), List.of("Madeira")),
                    new EmployeeSeed("jorge.guerra@teamfoundry.com", 105611121, "Jorge", "Guerra",
                            "+351910007555", "Portugal", "MALE", LocalDate.of(1991, 11, 7), "password123",
                            List.of("Pedreiro"), List.of("Orientacao para resultados", "Gestao de tempo"), List.of("Porto")),
                    new EmployeeSeed("ines.machado@teamfoundry.com", 105711121, "Ines", "Machado",
                            "+351910007666", "Portugal", "FEMALE", LocalDate.of(1993, 7, 29), "password123",
                            List.of("Eletricista"), List.of("Aprendizado rapido", "Organizacao"), List.of("Lisboa")),
                    new EmployeeSeed("ricardo.costa@teamfoundry.com", 105811121, "Ricardo", "Costa",
                            "+351910007777", "Portugal", "MALE", LocalDate.of(1989, 10, 16), "password123",
                            List.of("Soldador"), List.of("Gestao de conflitos", "Orientacao para resultados"), List.of("Faro")),
                    new EmployeeSeed("carolina.sousa@teamfoundry.com", 105911121, "Carolina", "Sousa",
                            "+351910007888", "Portugal", "FEMALE", LocalDate.of(1997, 4, 1), "password123",
                            List.of("Canalizador"), List.of("Atencao aos detalhes", "Comunicacao eficaz"), List.of("Braga")),
                    new EmployeeSeed("andre.goncalves@teamfoundry.com", 106011121, "Andre", "Goncalves",
                            "+351910007999", "Portugal", "MALE", LocalDate.of(1986, 5, 22), "password123",
                            List.of("Carpinteiro"), List.of("Proatividade", "Gestao de tempo"), List.of("Porto", "Lisboa")),
                    new EmployeeSeed("daniela.monteiro@teamfoundry.com", 106111121, "Daniela", "Monteiro",
                            "+351910008000", "Portugal", "FEMALE", LocalDate.of(1995, 1, 12), "password123",
                            List.of("Pedreiro"), List.of("Resolucao de problemas", "Trabalho em equipa"), List.of("Faro")),
                    new EmployeeSeed("tiago.cunha@teamfoundry.com", 106211121, "Tiago", "Cunha",
                            "+351910008111", "Portugal", "MALE", LocalDate.of(1990, 6, 6), "password123",
                            List.of("Eletricista"), List.of("Lideranca", "Aprendizado rapido"), List.of("Lisboa")),
                    new EmployeeSeed("mariana.alves@teamfoundry.com", 106311121, "Mariana", "Alves",
                            "+351910008222", "Portugal", "FEMALE", LocalDate.of(1994, 8, 18), "password123",
                            List.of("Soldador"), List.of("Pensamento critico", "Organizacao"), List.of("Porto")),
                    new EmployeeSeed("gustavo.reis@teamfoundry.com", 106411121, "Gustavo", "Reis",
                            "+351910008333", "Portugal", "MALE", LocalDate.of(1988, 2, 28), "password123",
                            List.of("Canalizador"), List.of("Gestao de conflitos", "Adaptabilidade"), List.of("Braga")),
                    new EmployeeSeed("barbara.lopes@teamfoundry.com", 106511121, "Barbara", "Lopes",
                            "+351910008444", "Portugal", "FEMALE", LocalDate.of(1996, 3, 14), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Atencao aos detalhes"), List.of("Madeira")),
                    new EmployeeSeed("eduardo.santos@teamfoundry.com", 106611121, "Eduardo", "Santos",
                            "+351910008555", "Portugal", "MALE", LocalDate.of(1987, 9, 9), "password123",
                            List.of("Pedreiro"), List.of("Orientacao para resultados", "Comunicacao eficaz"), List.of("Lisboa")),
                    new EmployeeSeed("vanessa.rodrigues@teamfoundry.com", 106711121, "Vanessa", "Rodrigues",
                            "+351910008666", "Portugal", "FEMALE", LocalDate.of(1993, 2, 20), "password123",
                            List.of("Eletricista"), List.of("Gestao de tempo", "Trabalho em equipa"), List.of("Porto")),
                    new EmployeeSeed("lucas.ferreira@teamfoundry.com", 106811121, "Lucas", "Ferreira",
                            "+351910008777", "Portugal", "MALE", LocalDate.of(1991, 12, 3), "password123",
                            List.of("Soldador"), List.of("Pensamento critico", "Proatividade"), List.of("Faro")),
                    new EmployeeSeed("helena.silva@teamfoundry.com", 106911121, "Helena", "Silva",
                            "+351910008888", "Portugal", "FEMALE", LocalDate.of(1997, 7, 7), "password123",
                            List.of("Canalizador"), List.of("Adaptabilidade", "Organizacao"), List.of("Lisboa", "Braga")),
                    new EmployeeSeed("filipe.ribeiro@teamfoundry.com", 107011121, "Filipe", "Ribeiro",
                            "+351910008999", "Portugal", "MALE", LocalDate.of(1986, 4, 27), "password123",
                            List.of("Carpinteiro"), List.of("Gestao de conflitos", "Orientacao para resultados"), List.of("Porto")),
                    new EmployeeSeed("juliana.martins@teamfoundry.com", 107111121, "Juliana", "Martins",
                            "+351910009000", "Portugal", "FEMALE", LocalDate.of(1994, 11, 19), "password123",
                            List.of("Pedreiro"), List.of("Resolucao de problemas", "Comunicacao eficaz"), List.of("Faro")),
                    new EmployeeSeed("sergio.costa@teamfoundry.com", 107211121, "Sergio", "Costa",
                            "+351910009111", "Portugal", "MALE", LocalDate.of(1989, 1, 23), "password123",
                            List.of("Eletricista"), List.of("Lideranca", "Gestao de tempo"), List.of("Lisboa")),
                    new EmployeeSeed("patricia.rocha@teamfoundry.com", 107311121, "Patricia", "Rocha",
                            "+351910009222", "Portugal", "FEMALE", LocalDate.of(1995, 5, 5), "password123",
                            List.of("Soldador"), List.of("Atencao aos detalhes", "Trabalho em equipa"), List.of("Porto", "Braga")),
                    new EmployeeSeed("renato.silva@teamfoundry.com", 107411121, "Renato", "Silva",
                            "+351910009333", "Portugal", "MALE", LocalDate.of(1988, 6, 30), "password123",
                            List.of("Canalizador"), List.of("Proatividade", "Resolucao de problemas"), List.of("Lisboa")),
                    new EmployeeSeed("marta.ferreira@teamfoundry.com", 107511121, "Marta", "Ferreira",
                            "+351910009444", "Portugal", "FEMALE", LocalDate.of(1996, 10, 1), "password123",
                            List.of("Carpinteiro"), List.of("Criatividade", "Organizacao"), List.of("Madeira")),
                    new EmployeeSeed("joao.almeida@teamfoundry.com", 107611121, "Joao", "Almeida",
                            "+351910009555", "Portugal", "MALE", LocalDate.of(1990, 8, 12), "password123",
                            List.of("Pedreiro"), List.of("Orientacao para resultados", "Trabalho em equipa"), List.of("Porto", "Lisboa"))
            );

            List<EmployeeRole> functionRelations = new ArrayList<>();
            List<EmployeeSkill> competenceRelations = new ArrayList<>();
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
                    PrefRole function = functionsByName.get(functionName);
                    if (function == null) {
                        LOGGER.warn("Function {} not found; skipping relation for {}.", functionName, seed.email());
                        return;
                    }
                    EmployeeRole relation = new EmployeeRole();
                    relation.setEmployee(savedEmployee);
                    relation.setFunction(function);
                    functionRelations.add(relation);
                });

                seed.competences().forEach(competenceName -> {
                    PrefSkill prefSkill = competencesByName.get(competenceName);
                    if (prefSkill == null) {
                        LOGGER.warn("PrefSkill {} not found; skipping relation for {}.", competenceName, seed.email());
                        return;
                    }
                    EmployeeSkill relation = new EmployeeSkill();
                    relation.setEmployee(savedEmployee);
                    relation.setPrefSkill(prefSkill);
                    competenceRelations.add(relation);
                });

                seed.geoAreas().forEach(areaName -> {
                    PrefGeoArea geoArea = geoAreasByName.get(areaName);
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
                employeeRoleRepository.saveAll(functionRelations);
                LOGGER.info("Seeded {} employee-function relations.", functionRelations.size());
            }
            if (!competenceRelations.isEmpty()) {
                employeeSkillRepository.saveAll(competenceRelations);
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

    private Map<String, PrefRole> loadFunctions(PrefRoleRepository repository) {
        Map<String, PrefRole> functions = new HashMap<>();
        repository.findAll().forEach(function -> functions.put(function.getName(), function));
        return functions;
    }

    private Map<String, PrefSkill> loadCompetences(PrefSkillRepository repository) {
        Map<String, PrefSkill> competences = new HashMap<>();
        repository.findAll().forEach(competence -> competences.put(competence.getName(), competence));
        return competences;
    }

    private Map<String, PrefGeoArea> loadGeoAreas(PrefGeoAreaRepository repository) {
        Map<String, PrefGeoArea> geoAreas = new HashMap<>();
        repository.findAll().forEach(area -> geoAreas.put(area.getName(), area));
        return geoAreas;
    }
}

