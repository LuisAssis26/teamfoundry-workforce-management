package com.teamfoundry.backend.auth.config;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.auth.dto.login.LoginResult;
import com.teamfoundry.backend.auth.service.login.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final AccountRepository accountRepository;
    private final AuthService authService;

    @Value("${app.oauth2.front-success-url:http://localhost:5173/oauth/google/callback}")
    private String frontSuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth) ||
                !(oauth.getPrincipal() instanceof DefaultOAuth2User user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticação inválida");
            return;
        }

        Map<String, Object> attrs = user.getAttributes();
        String email = getString(attrs, "email");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google não devolveu email.");
            return;
        }

        Account account = findOrCreateAccount(attrs, email.toLowerCase(Locale.ROOT));
        LoginResult tokens = authService.loginWithAccount(account, true);

        if (tokens.refreshToken() != null) {
            ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                    .httpOnly(true)
                    .secure(false) // ajuste para true em produção com HTTPS
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(tokens.refreshMaxAgeSeconds() >= 0 ? tokens.refreshMaxAgeSeconds() : -1)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        String target = UriComponentsBuilder.fromHttpUrl(frontSuccessUrl)
                .queryParam("accessToken", tokens.response().accessToken())
                .queryParam("userType", tokens.response().userType())
                .build()
                .toUriString();
        response.sendRedirect(target);
    }

    private Account findOrCreateAccount(Map<String, Object> attrs, String email) {
        Optional<Account> existing = accountRepository.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        String givenName = getString(attrs, "given_name");
        String familyName = getString(attrs, "family_name");
        String fullName = getString(attrs, "name");

        EmployeeAccount account = new EmployeeAccount();
        account.setEmail(email);
        account.setRole(UserType.EMPLOYEE);
        account.setVerified(true);
        account.setRegistrationStatus(RegistrationStatus.COMPLETED);
        account.setDeactivated(false);
        account.setPassword(generatePlaceholderPassword());
        if (givenName != null) account.setName(givenName);
        else if (fullName != null) account.setName(fullName);
        if (familyName != null) account.setSurname(familyName);

        EmployeeAccount saved = employeeAccountRepository.save(account);
        return saved;
    }

    private String getString(Map<String, Object> attrs, String key) {
        Object val = attrs.get(key);
        return val != null ? val.toString() : null;
    }

    private String generatePlaceholderPassword() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        String raw = java.util.Base64.getEncoder().encodeToString(buf);
        return new BCryptPasswordEncoder().encode(raw);
    }
}
