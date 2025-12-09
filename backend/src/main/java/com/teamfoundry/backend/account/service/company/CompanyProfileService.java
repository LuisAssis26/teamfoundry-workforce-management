package com.teamfoundry.backend.account.service.company;

import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccountManager;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.auth.service.VerificationEmailService;
import com.teamfoundry.backend.account.dto.company.profile.CompanyManagerUpdateRequest;
import com.teamfoundry.backend.account.dto.company.profile.CompanyManagerVerifyRequest;
import com.teamfoundry.backend.account.dto.company.preferences.CompanyProfileResponse;
import com.teamfoundry.backend.auth.model.tokens.AuthToken;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Serviço de leitura/atualização dos dados de perfil da empresa autenticada.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyProfileService {

    private final CompanyAccountRepository companyAccountRepository;
    private final CompanyAccountOwnerRepository ownerRepository;
    private final AuthTokenRepository authTokenRepository;
    private final VerificationEmailService verificationEmailService;

    @Value("${app.registration.verification.expiration-minutes:30}")
    private long verificationExpirationMinutes;

    private final Random random = new Random();

    /**
     * Recupera o perfil completo da empresa autenticada (dados da conta + responsável).
     */
    public CompanyProfileResponse getProfile(String email) {
        CompanyAccount account = resolveCompany(email);
        CompanyAccountManager manager = ownerRepository.findByCompanyAccount_Email(email)
                .orElse(null);
        return toResponse(account, manager);
    }

    /**
     * Atualiza apenas os dados do responsável (sem alterar email).
     */
    @Transactional
    public CompanyProfileResponse updateManager(String email, CompanyManagerUpdateRequest request) {
        CompanyAccount account = resolveCompany(email);
        CompanyAccountManager manager = ownerRepository.findByCompanyAccount_Email(email)
                .orElseGet(() -> buildDefaultManager(account));

        manager.setName(request.getName());
        manager.setPhone(request.getPhone());
        manager.setPosition(request.getPosition());
        ownerRepository.save(manager);

        return toResponse(account, manager);
    }

    /**
     * Envia código de verificação para um novo email de responsável.
     */
    @Transactional
    public void sendVerificationCode(String accountEmail, String newEmail) {
        CompanyAccount account = resolveCompany(accountEmail);
        authTokenRepository.deleteAllByUser(account);
        AuthToken token = buildVerificationToken(account);
        authTokenRepository.save(token);
        verificationEmailService.sendVerificationCode(newEmail.trim().toLowerCase(), token.getToken());
    }

    /**
     * Confirma o código e aplica o novo email + dados do responsável.
     */
    @Transactional
    public CompanyProfileResponse verifyAndUpdateManager(String accountEmail, CompanyManagerVerifyRequest request) {
        CompanyAccount account = resolveCompany(accountEmail);
        AuthToken token = authTokenRepository.findByAccountAndCode(account, request.getCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido."));

        if (token.getExpireAt().before(Timestamp.from(Instant.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O código de verificação expirou.");
        }

        CompanyAccountManager manager = ownerRepository.findByCompanyAccount_Email(accountEmail)
                .orElseGet(() -> buildDefaultManager(account));

        manager.setName(request.getName());
        manager.setPhone(request.getPhone());
        manager.setPosition(request.getPosition());
        manager.setEmail(request.getNewEmail().trim().toLowerCase());
        ownerRepository.save(manager);

        authTokenRepository.delete(token);
        return toResponse(account, manager);
    }

    private CompanyAccountManager buildDefaultManager(CompanyAccount account) {
        CompanyAccountManager created = new CompanyAccountManager();
        created.setCompanyAccount(account);
        created.setEmail(account.getEmail());
        return created;
    }

    private AuthToken buildVerificationToken(CompanyAccount account) {
        AuthToken token = new AuthToken();
        token.setUser(account);
        token.setToken(generateNumericCode(6));
        Instant now = Instant.now();
        token.setCreatedAt(Timestamp.from(now));
        token.setExpireAt(Timestamp.from(now.plus(verificationExpirationMinutes, ChronoUnit.MINUTES)));
        return token;
    }

    private String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private CompanyProfileResponse toResponse(CompanyAccount account, CompanyAccountManager manager) {
        return CompanyProfileResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .nif(account.getNif())
                .email(account.getEmail())
                .address(account.getAddress())
                .country(account.getCountry())
                .phone(account.getPhone())
                .website(account.getWebsite())
                .description(account.getDescription())
                .status(account.isStatus())
                .manager(manager == null ? null : CompanyProfileResponse.Manager.builder()
                        .name(manager.getName())
                        .email(manager.getEmail())
                        .phone(manager.getPhone())
                        .position(manager.getPosition())
                        .build())
                .build();
    }

    private CompanyAccount resolveCompany(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação requerida.");
        }
        return companyAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));
    }
}
