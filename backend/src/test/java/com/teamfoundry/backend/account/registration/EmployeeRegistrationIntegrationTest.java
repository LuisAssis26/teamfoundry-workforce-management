package com.teamfoundry.backend.account.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import com.teamfoundry.backend.account.model.preferences.PrefRole;
import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import com.teamfoundry.backend.account.repository.preferences.PrefSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefRoleRepository;
import com.teamfoundry.backend.account.repository.preferences.PrefGeoAreaRepository;
import com.teamfoundry.backend.auth.model.tokens.AuthToken;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import com.teamfoundry.backend.auth.service.VerificationEmailService;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.teamfoundry.backend.superadmin.config.home.HomeLoginContentInitializer;



import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Employee registration flow")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("removal") // MockBean deprecation warnings in Spring Boot 3.4
class EmployeeRegistrationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EmployeeAccountRepository employeeAccountRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthTokenRepository authTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Autowired
    PrefRoleRepository prefRoleRepository;
    @Autowired
    PrefGeoAreaRepository prefGeoAreaRepository;
    @Autowired
    PrefSkillRepository prefSkillRepository;
    @Autowired
    EmployeeRoleRepository employeeRoleRepository;
    @Autowired EmployeeGeoAreaRepository employeeGeoAreaRepository;
    @Autowired
    EmployeeSkillRepository employeeSkillRepository;

    @MockBean
    VerificationEmailService verificationEmailService;


    private final String email = "Candidate@Test.com";
    private final String password = "StrongPass123";
    private final LocalDate birthDate = LocalDate.of(2000, 1, 15);

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        employeeRoleRepository.deleteAll();
        employeeGeoAreaRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        prefSkillRepository.deleteAll();
        prefGeoAreaRepository.deleteAll();
        prefRoleRepository.deleteAll();
        employeeAccountRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Step1 creates pending employee account")
    void step1SuccessfulRegistration() throws Exception {
        performStep1(email, password);

        EmployeeAccount created = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        assertThat(created.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(created.getRole()).isEqualTo(UserType.EMPLOYEE);
        assertThat(created.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(created.isVerified()).isFalse();
        assertThat(created.getPassword()).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, created.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Step1 allows restarting registration when account is still pending")
    void step1AllowsRestartForPendingAccount() throws Exception {
        // primeiro registo
        performStep1(email, password);
        EmployeeAccount first = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        
        // segundo step1 com mesmo email (recomeçar)
        performStep1(email, password); // agora esperamos 201 outra vez, não 4xx

        EmployeeAccount second = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();

        // continua a haver só 1 conta para aquele email
        assertThat(employeeAccountRepository.count()).isEqualTo(1);

        // continua PENDING e inativa
        assertThat(second.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(second.isVerified()).isFalse();
    }


    @Test
    @DisplayName("Step2 updates personal data")
    void step2UpdatesPersonalData() throws Exception {
        performStep1(email, password);
        performStep2(email);

        EmployeeAccount account = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        assertThat(account.getName()).isEqualTo("Alice");
        assertThat(account.getSurname()).isEqualTo("Doe");
        assertThat(account.getNationality()).isEqualTo("Portugal");
        assertThat(account.getBirthDate()).isEqualTo(birthDate);
        assertThat(account.getPhone()).isEqualTo("+351987654321");
        assertThat(account.getNif()).isEqualTo(123456789);
    }

    @Test
    @DisplayName("Step3 stores preferences and generates token")
    void step3PreferencesAndToken() throws Exception {
        performStep1(email, password);
        performStep2(email);

        String roleName = "Developer";
        String areaName = "Lisbon";
        String skillName = "Java";
        seedOptionData(roleName, areaName, skillName);

        performStep3(email, roleName, areaName, skillName, true, status().isOk(), true);

        EmployeeAccount account = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        assertThat(employeeRoleRepository.findFirstByEmployee(account))
                .isPresent()
                .get()
                .extracting(rel -> rel.getFunction().getName())
                .isEqualTo(roleName);
        assertThat(employeeGeoAreaRepository.findByEmployee(account))
                .extracting(rel -> rel.getGeoArea().getName())
                .containsExactly(areaName);
        assertThat(employeeSkillRepository.findByEmployee(account))
                .extracting(rel -> rel.getPrefSkill().getName())
                .containsExactly(skillName);

        long tokensForAccount = authTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(account.getId()))
                .count();
        assertThat(tokensForAccount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Step3 fails when terms not accepted")
    void step3TermsNotAccepted() throws Exception {
        performStep1(email, password);
        performStep2(email);

        String roleName = "Analyst";
        String areaName = "Porto";
        String skillName = "SQL";
        seedOptionData(roleName, areaName, skillName);

        performStep3(email, roleName, areaName, skillName, false, status().isBadRequest(), false);

        EmployeeAccount account = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        assertThat(employeeRoleRepository.findFirstByEmployee(account)).isEmpty();
        assertThat(employeeGeoAreaRepository.findByEmployee(account)).isEmpty();
        assertThat(employeeSkillRepository.findByEmployee(account)).isEmpty();
        assertThat(authTokenRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Step4 activates account with correct verification code")
    void step4CompletesVerification() throws Exception {
        performStep1(email, password);
        performStep2(email);

        String roleName = "Tester";
        String areaName = "Braga";
        String skillName = "Selenium";
        seedOptionData(roleName, areaName, skillName);
        performStep3(email, roleName, areaName, skillName, true, status().isOk(), true);

        EmployeeAccount account = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        AuthToken token = authTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow();

        performStep4(email, token.getToken(), status().isOk());

        EmployeeAccount activated = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        assertThat(activated.isVerified()).isTrue();
        assertThat(activated.getRegistrationStatus()).isEqualTo(RegistrationStatus.COMPLETED);
        assertThat(authTokenRepository.findByAccountAndCode(activated, token.getToken())).isEmpty();
    }

    @Test
    @DisplayName("Step4 keeps account inactive when code is wrong")
    void step4WrongCodeKeepsAccountInactive() throws Exception {
        performStep1(email, password);
        performStep2(email);

        String roleName = "Architect";
        String areaName = "Coimbra";
        String skillName = "AWS";
        seedOptionData(roleName, areaName, skillName);
        performStep3(email, roleName, areaName, skillName, true, status().isOk(), true);

        EmployeeAccount account = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        AuthToken token = authTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow();

        performStep4(email, "000000", status().is4xxClientError());

        EmployeeAccount unchanged = employeeAccountRepository.findByEmail(email.toLowerCase()).orElseThrow();
        assertThat(unchanged.isVerified()).isFalse();
        assertThat(unchanged.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(authTokenRepository.findByAccountAndCode(account, token.getToken())).isPresent();
    }

    private void performStep1(String email, String password) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));

        mockMvc.perform(post("/api/employee/register/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Conta criada com sucesso. Continue para o passo 2."));
    }

    private void performStep2(String email) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "firstName", "Alice",
                "lastName", "Doe",
                "nationality", "Portugal",
                "birthDate", birthDate,
                "phone", "+351987654321",
                "nif", 123456789
        ));

        mockMvc.perform(post("/api/employee/register/step2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dados atualizados."));
    }

    private void performStep3(String email, String role, String area, String skill, boolean termsAccepted, ResultMatcher expectedStatus, boolean expectSuccessPayload) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "role", role,
                "areas", List.of(area),
                "skills", List.of(skill),
                "termsAccepted", termsAccepted
        ));

        ResultActions actions = mockMvc.perform(post("/api/employee/register/step3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(expectedStatus);

        if (expectSuccessPayload) {
            actions.andExpect(jsonPath("$.message").exists());
        } else {
            actions.andExpect(jsonPath("$.error").exists());
        }
    }

    private void performStep4(String email, String verificationCode, ResultMatcher expectedStatus) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "verificationCode", verificationCode
        ));

        mockMvc.perform(post("/api/employee/register/step4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(expectedStatus);
    }

    private void seedOptionData(String functionName, String areaName, String competenceName) {
        var function = new PrefRole();
        function.setName(functionName);
        prefRoleRepository.save(function);

        var area = new PrefGeoArea();
        area.setName(areaName);
        prefGeoAreaRepository.save(area);

        PrefSkill prefSkill = new PrefSkill();
        prefSkill.setName(competenceName);
        prefSkillRepository.save(prefSkill);
    }
}

