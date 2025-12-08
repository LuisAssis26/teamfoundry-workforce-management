package com.teamfoundry.backend.account.service;

import com.teamfoundry.backend.account.dto.GenericResponse;
import com.teamfoundry.backend.account.dto.employee.Step1Request;
import com.teamfoundry.backend.account.dto.employee.Step2Request;
import com.teamfoundry.backend.account.dto.employee.Step3Request;
import com.teamfoundry.backend.account.dto.employee.Step4Request;
import com.teamfoundry.backend.account.dto.employee.VerificationResendRequest;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.EmployeeAccountRepository;
import com.teamfoundry.backend.account.service.exception.EmployeeRegistrationException;
import com.teamfoundry.backend.account.service.exception.DuplicateEmailException;
import com.teamfoundry.backend.account_options.enums.DocumentType;
import com.teamfoundry.backend.account_options.model.employee.Competence;
import com.teamfoundry.backend.account_options.model.employee.EmployeeCompetence;
import com.teamfoundry.backend.account_options.model.employee.EmployeeDocument;
import com.teamfoundry.backend.account_options.model.employee.EmployeeFunction;
import com.teamfoundry.backend.account_options.model.employee.EmployeeGeoArea;
import com.teamfoundry.backend.account_options.model.employee.Function;
import com.teamfoundry.backend.account_options.model.employee.GeoArea;
import com.teamfoundry.backend.account_options.repository.employee.CompetenceRepository;
import com.teamfoundry.backend.account_options.repository.employee.DocumentRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeCompetenceRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeFunctionRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account_options.repository.employee.FunctionRepository;
import com.teamfoundry.backend.account_options.repository.employee.GeoAreaRepository;
import com.teamfoundry.backend.security.model.AuthToken;
import com.teamfoundry.backend.security.repository.AuthTokenRepository;
import com.teamfoundry.backend.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeRegistrationService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final AccountRepository accountRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FunctionRepository functionRepository;
    private final EmployeeFunctionRepository employeeFunctionRepository;
    private final CompetenceRepository competenceRepository;
    private final EmployeeCompetenceRepository employeeCompetenceRepository;
    private final GeoAreaRepository geoAreaRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;
    private final DocumentRepository documentRepository;
    private final VerificationEmailService verificationEmailService;
    private final CloudinaryService cloudinaryService;

    @Value("${app.registration.verification.expiration-minutes:30}")
    private long verificationExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public GenericResponse handleStep1(Step1Request request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        accountRepository.findByEmail(normalizedEmail)
                .filter(existing -> !(existing instanceof EmployeeAccount))
                .ifPresent(existing -> {
                    throw new EmployeeRegistrationException(
                            "O email informado já está associado a outro tipo de conta.",
                            HttpStatus.CONFLICT
                    );
                });
        Optional<EmployeeAccount> existingAccountOpt = employeeAccountRepository.findByEmail(normalizedEmail);

        EmployeeAccount account = existingAccountOpt.orElseGet(EmployeeAccount::new);

        if (existingAccountOpt.isPresent()) {
            if (Boolean.TRUE.equals(account.isVerified()) || RegistrationStatus.COMPLETED.equals(account.getRegistrationStatus())) {
                throw new DuplicateEmailException("O email informado já está associado a uma conta ativa.");
            }
            resetAccountForRestart(account);
        } else {
            account.setEmail(normalizedEmail);
            account.setRole(UserType.EMPLOYEE);
        }

        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRegistrationStatus(RegistrationStatus.PENDING);
        account.setVerified(false);

        try {
            employeeAccountRepository.save(account);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao criar conta para {}", normalizedEmail, ex);
            throw new EmployeeRegistrationException(
                    "O email informado já está associado a outra conta.",
                    HttpStatus.CONFLICT
            );
        }

        return GenericResponse.success("Conta criada com sucesso. Continue para o passo 2.");
    }

    @Transactional
    public GenericResponse handleStep2(Step2Request request) {
        EmployeeAccount account = findAccountByEmail(request.getEmail());

        account.setName(request.getFirstName());
        account.setSurname(request.getLastName());
        account.setPhone(request.getPhone());
        account.setNationality(request.getNationality());
        account.setBirthDate(request.getBirthDate());
        account.setNif(request.getNif());

        CloudinaryService.UploadResult cvUpload = storeCvIfPresent(account, request.getCvFile(), request.getCvFileName());
        employeeAccountRepository.save(account);

        if (cvUpload != null) {
            saveDocument(account, DocumentType.CURRICULUM, cvUpload.getPublicId(), request.getCvFileName());
        }

        return GenericResponse.success("Dados atualizados.");
    }

    @Transactional
    public GenericResponse handleStep3(Step3Request request) {
        EmployeeAccount account = findAccountByEmail(request.getEmail());

        if (Boolean.FALSE.equals(request.getTermsAccepted())) {
            throw new EmployeeRegistrationException("É necessário aceitar os termos e condições.", HttpStatus.BAD_REQUEST);
        }

        applyFunctionPreference(account, request.getRole());
        applyCompetencePreferences(account, request.getSkills());
        applyGeoAreaPreferences(account, request.getAreas());

        issueVerificationCode(account);
        return GenericResponse.success("Preferências guardadas. Código enviado para o seu email.");
    }

    @Transactional
    public GenericResponse handleStep4(Step4Request request) {
        EmployeeAccount account = findAccountByEmail(request.getEmail());

        AuthToken token = authTokenRepository.findByAccountAndCode(account, request.getVerificationCode())
                .orElseThrow(() -> new EmployeeRegistrationException("Código de verificação inválido.", HttpStatus.BAD_REQUEST));

        if (token.getExpireAt().before(Timestamp.from(Instant.now()))) {
            throw new EmployeeRegistrationException("O código de verificação expirou.", HttpStatus.BAD_REQUEST);
        }

        account.setVerified(true);
        account.setRegistrationStatus(RegistrationStatus.COMPLETED);
        employeeAccountRepository.save(account);

        authTokenRepository.delete(token);
        return GenericResponse.success("Conta verificada e ativa.");
    }

    @Transactional
    public GenericResponse resendVerificationCode(VerificationResendRequest request) {
        EmployeeAccount account = findAccountByEmail(request.email());
        issueVerificationCode(account);
        return GenericResponse.success("Novo código enviado para o email informado.");
    }

    private EmployeeAccount findAccountByEmail(String email) {
        return employeeAccountRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new EmployeeRegistrationException("Conta não encontrada para o email informado.", HttpStatus.NOT_FOUND));
    }

    private void issueVerificationCode(EmployeeAccount account) {
        authTokenRepository.deleteAllByUser(account);
        AuthToken token = buildVerificationToken(account);
        authTokenRepository.save(token);
        verificationEmailService.sendVerificationCode(account.getEmail(), token.getToken());
        log.info("Generated verification code {} for employee {}", token.getToken(), account.getEmail());
    }

    private AuthToken buildVerificationToken(EmployeeAccount account) {
        AuthToken token = new AuthToken();
        token.setUser(account);
        token.setToken(generateNumericCode(6));
        Instant now = Instant.now();
        token.setCreatedAt(Timestamp.from(now));
        token.setExpireAt(Timestamp.from(now.plus(verificationExpirationMinutes, ChronoUnit.MINUTES)));
        return token;
    }

    private void saveDocument(EmployeeAccount account, DocumentType type, String publicId, String fileName) {
        EmployeeDocument document = documentRepository.findByEmployeeAndType(account, type)
                .orElseGet(() -> {
                    EmployeeDocument doc = new EmployeeDocument();
                    doc.setEmployee(account);
                    doc.setType(type);
                    return doc;
                });
        cloudinaryService.delete(document.getPublicId());
        document.setPublicId(publicId);
        document.setFileName(fileName);
        documentRepository.save(document);
    }

    private CloudinaryService.UploadResult storeCvIfPresent(EmployeeAccount account, String cvPayload, String originalName) {
        if (!StringUtils.hasText(cvPayload)) {
            return null;
        }

        try {
            CloudinaryService.UploadResult upload = cloudinaryService.uploadBase64(cvPayload, "curriculum", originalName);
            log.info("Stored CV for {} at {}", account.getEmail(), upload.getPublicId());
            return upload;
        } catch (IllegalArgumentException ex) {
            throw new EmployeeRegistrationException("O ficheiro de CV enviado e invalido.", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Error storing CV file for {}", account.getEmail(), ex);
            throw new EmployeeRegistrationException("Nao foi possivel guardar o CV. Tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }

    private void resetAccountForRestart(EmployeeAccount account) {
        if (account.getId() != null) {
            removeCvIfExists(account);
            employeeFunctionRepository.deleteByEmployee(account);
            employeeCompetenceRepository.deleteByEmployee(account);
            employeeGeoAreaRepository.deleteByEmployee(account);
        }
        account.setName(null);
        account.setSurname(null);
        account.setPhone(null);
        account.setNationality(null);
        account.setGender(null);
        account.setBirthDate(null);
        account.setNif(null);
    }

    private void removeCvIfExists(EmployeeAccount account) {
        if (account.getId() == null) {
            return;
        }
        documentRepository.findByEmployeeAndType(account, DocumentType.CURRICULUM).ifPresent(doc -> {
            cloudinaryService.delete(doc.getPublicId());
            documentRepository.delete(doc);
        });
    }

    private void applyFunctionPreference(EmployeeAccount account, String functionName) {
        employeeFunctionRepository.deleteByEmployee(account);

        if (!StringUtils.hasText(functionName)) {
            throw new EmployeeRegistrationException("Função preferencial é obrigatória.", HttpStatus.BAD_REQUEST);
        }

        Function function = functionRepository.findByName(functionName.trim())
                .orElseThrow(() -> new EmployeeRegistrationException("Função não encontrada: " + functionName, HttpStatus.BAD_REQUEST));

        EmployeeFunction relation = new EmployeeFunction();
        relation.setEmployee(account);
        relation.setFunction(function);
        employeeFunctionRepository.save(relation);
    }

    private void applyCompetencePreferences(EmployeeAccount account, List<String> skills) {
        employeeCompetenceRepository.deleteByEmployee(account);

        if (Objects.isNull(skills) || skills.isEmpty()) {
            throw new EmployeeRegistrationException("Pelo menos uma competência deve ser selecionada.", HttpStatus.BAD_REQUEST);
        }

        List<EmployeeCompetence> relations = skills.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .map(name -> {
                    Competence competence = competenceRepository.findByName(name)
                            .orElseThrow(() -> new EmployeeRegistrationException("Competência não encontrada: " + name, HttpStatus.BAD_REQUEST));
                    EmployeeCompetence relation = new EmployeeCompetence();
                    relation.setEmployee(account);
                    relation.setCompetence(competence);
                    return relation;
                })
                .toList();

        employeeCompetenceRepository.saveAll(relations);
    }

    private void applyGeoAreaPreferences(EmployeeAccount account, List<String> areas) {
        employeeGeoAreaRepository.deleteByEmployee(account);

        if (Objects.isNull(areas) || areas.isEmpty()) {
            throw new EmployeeRegistrationException("Selecione pelo menos uma área geográfica.", HttpStatus.BAD_REQUEST);
        }

        List<EmployeeGeoArea> relations = areas.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .map(name -> {
                    GeoArea area = geoAreaRepository.findByName(name)
                            .orElseThrow(() -> new EmployeeRegistrationException("Área geográfica não encontrada: " + name, HttpStatus.BAD_REQUEST));
                    EmployeeGeoArea relation = new EmployeeGeoArea();
                    relation.setEmployee(account);
                    relation.setGeoArea(area);
                    return relation;
                })
                .toList();

        employeeGeoAreaRepository.saveAll(relations);
    }
}
