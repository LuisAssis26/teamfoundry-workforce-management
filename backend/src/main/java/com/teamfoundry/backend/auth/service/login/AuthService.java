package com.teamfoundry.backend.auth.service.login;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.auth.service.VerificationEmailService;
import com.teamfoundry.backend.auth.dto.login.LoginRequest;
import com.teamfoundry.backend.auth.dto.login.LoginResponse;
import com.teamfoundry.backend.auth.dto.login.LoginResult;
import com.teamfoundry.backend.auth.model.tokens.AuthToken;
import com.teamfoundry.backend.auth.model.tokens.PasswordResetToken;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
import com.teamfoundry.backend.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int REMEMBER_ME_DAYS = 30;
    private static final int SESSION_REFRESH_DAYS = 1; // refresh de sessão (cookie de sessão)

    private final AdminAccountRepository adminAccountRepository;
    private final CompanyAccountRepository companyAccountRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository authTokenRepository;
    private final JwtService jwtService;
    private final VerificationEmailService verificationEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Procura um utilizador em todos os contextos (admin, empresa, colaborador) e gera o par de tokens
     * que o frontend precisa para iniciar sessão.
     */
    public LoginResult login(LoginRequest request) {
        String identifier = request.email().trim();
        String normalizedEmail = identifier.toLowerCase();
        boolean remember = Boolean.TRUE.equals(request.rememberMe());

        log.info("Tentativa de login recebida para {}", identifier);

        Optional<LoginResult> admin = adminAccountRepository.findByUsernameIgnoreCase(identifier)
                .map(account -> validateAdmin(account, request.password()));
        if (admin.isPresent()) {
            return admin.get();
        }

        Optional<LoginResult> company = companyAccountRepository.findByEmail(normalizedEmail)
                .map(account -> validateAccount(account, request.password(), remember));
        if (company.isPresent()) {
            return company.get();
        }

        Optional<LoginResult> employee = employeeAccountRepository.findByEmail(normalizedEmail)
                .map(account -> validateEmployee(account, request.password(), remember));
        if (employee.isPresent()) {
            return employee.get();
        }

        log.warn("Login falhou para {}: utilizador não encontrado", identifier);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    private LoginResult validateAdmin(AdminAccount adminAccount, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, adminAccount.getPassword())) {
            log.warn("Password incorreta para administrador {}", adminAccount.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        LoginResponse resp = new LoginResponse(UserType.ADMIN.name(), "Login efetuado com sucesso", null, 0);
        return new LoginResult(resp, null, 0);
    }

    private LoginResult validateAccount(Account account, String rawPassword, boolean remember) {
        ensurePasswordMatches(account, rawPassword);
        ensureAccountIsActive(account);
        String access = jwtService.generateToken(account.getEmail(), account.getRole().name(), account.getId());
        LoginResponse resp = new LoginResponse(account.getRole().name(), "Login efetuado com sucesso", access, jwtService.getExpirationSeconds());
        if (remember) {
            String refresh = issueRefreshToken(account, REMEMBER_ME_DAYS);
            return new LoginResult(resp, refresh, REMEMBER_ME_DAYS * 24 * 60 * 60);
        }
        String refresh = issueRefreshToken(account, SESSION_REFRESH_DAYS);
        // maxAge -1 => cookie de sessão (dura enquanto o browser estiver aberto)
        return new LoginResult(resp, refresh, -1);
    }

    private LoginResult validateEmployee(EmployeeAccount employeeAccount, String rawPassword, boolean remember) {
        ensurePasswordMatches(employeeAccount, rawPassword);
        ensureAccountIsActive(employeeAccount);
        String access = jwtService.generateToken(employeeAccount.getEmail(), employeeAccount.getRole().name(), employeeAccount.getId());
        LoginResponse resp = new LoginResponse(employeeAccount.getRole().name(), "Login efetuado com sucesso", access, jwtService.getExpirationSeconds());
        if (remember) {
            String refresh = issueRefreshToken(employeeAccount, REMEMBER_ME_DAYS);
            return new LoginResult(resp, refresh, REMEMBER_ME_DAYS * 24 * 60 * 60);
        }
        String refresh = issueRefreshToken(employeeAccount, SESSION_REFRESH_DAYS);
        return new LoginResult(resp, refresh, -1);
    }

    private String issueRefreshToken(Account user, int days) {
        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(Timestamp.from(Instant.now()));
        token.setExpireAt(Timestamp.from(Instant.now().plus(days, ChronoUnit.DAYS)));
        authTokenRepository.save(token);
        return token.getToken();
    }

    private void ensurePasswordMatches(Account account, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, account.getPassword())) {
            log.warn("Password incorreta para {}", account.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
    }

    private void ensureAccountIsActive(Account account) {
        if (account.isDeactivated()) {
            log.warn("Conta {} está desativada", account.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta desativada. Contacte o suporte.");
        }
        if (!account.isVerified()) {
            log.warn("Conta {} ainda não está verificada", account.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta ainda não foi verificada");
        }
    }

    /**
     * Passo 1 do fluxo de recuperação: valida se a conta é elegível e gera um novo código descartando os anteriores.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Account account = accountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta não encontrada."));
        ensureAccountEligibleForReset(account);

        passwordResetTokenRepository.deleteByUser(account);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(account);
        String code = generateResetCode();
        prt.setToken(code);
        prt.setCreatedAt(Timestamp.from(Instant.now()));
        prt.setExpireAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenRepository.save(prt);

        verificationEmailService.sendPasswordResetCode(account.getEmail(), code);
    }

    /**
     * Passo 3 do fluxo: valida que o código está ativo, aplica a nova password e expira os tokens antigos.
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        var user = accountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email não existe, crie uma conta"));
        ensureAccountEligibleForReset(user);

        PasswordResetToken prt = passwordResetTokenRepository.findByUserAndToken(user, code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorreto, reveja a sua caixa de correio"));
        if (prt.getExpireAt().before(Timestamp.from(Instant.now()))) {
            passwordResetTokenRepository.delete(prt);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado, recomeçe o processo");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nova password deve ser diferente da atual");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(user);

        // Invalidate used token(s)
        passwordResetTokenRepository.deleteByUser(user);
    }

    /**
     * Passo 2 do fluxo: confirma via backend se o código ainda é válido antes de abrir o formulário de password.
     */
    @Transactional(readOnly = true)
    public void validateResetCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        var user = accountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email não encontrado"));
        ensureAccountEligibleForReset(user);

        PasswordResetToken prt = passwordResetTokenRepository.findByUserAndToken(user, code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorreto, reveja a sua caixa de correio"));
        if (prt.getExpireAt().before(Timestamp.from(Instant.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado, recomeçe o processo");
        }
    }

    private String generateResetCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    /**
     * Garante que apenas contas completamente registadas e aprovadas (quando aplicável) podem redefinir password.
     */
    private void ensureAccountEligibleForReset(Account account) {
        if (account.getRegistrationStatus() != RegistrationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta conta ainda não concluiu o registo.");
        }
        if (!account.isVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A conta ainda não foi verificada.");
        }
        if (account instanceof CompanyAccount companyAccount && !companyAccount.isStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A conta da empresa aguarda aprovação do administrador.");
        }
    }

    public LoginResponse refresh(String refreshToken) {
        var tokenOpt = authTokenRepository.findByToken(refreshToken);
        var token = tokenOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));
        if (token.getExpireAt().before(Timestamp.from(Instant.now()))) {
            authTokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        Account user = token.getUser();
        if (!user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta inativa");
        }
        String access = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return new LoginResponse(user.getRole().name(), "Token renovado", access, jwtService.getExpirationSeconds());
    }

    public void revokeRefresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        authTokenRepository.findByToken(refreshToken).ifPresent(authTokenRepository::delete);
    }
    /**
     * Autentica um utilizador existente sem validar password (usado em login social/OAuth).
     */
    public LoginResult loginWithAccount(Account account, boolean remember) {
        ensureAccountIsActive(account);
        String access = jwtService.generateToken(account.getEmail(), account.getRole().name(), account.getId());
        LoginResponse resp = new LoginResponse(account.getRole().name(), "Login efetuado com sucesso", access, jwtService.getExpirationSeconds());
        if (remember) {
            String refresh = issueRefreshToken(account, REMEMBER_ME_DAYS);
            return new LoginResult(resp, refresh, REMEMBER_ME_DAYS * 24 * 60 * 60);
        }
        String refresh = issueRefreshToken(account, SESSION_REFRESH_DAYS);
        return new LoginResult(resp, refresh, -1);
    }
}

