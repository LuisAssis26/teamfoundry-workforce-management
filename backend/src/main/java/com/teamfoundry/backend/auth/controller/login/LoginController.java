package com.teamfoundry.backend.auth.controller.login;

import com.teamfoundry.backend.auth.dto.login.LoginRequest;
import com.teamfoundry.backend.auth.dto.login.LoginResponse;
import com.teamfoundry.backend.auth.controller.password.ForgotPasswordRequest;
import com.teamfoundry.backend.auth.controller.password.ResetPasswordRequest;
import com.teamfoundry.backend.auth.dto.login.LoginResult;
import com.teamfoundry.backend.auth.service.login.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    /**
     * Endpoint de login público. Devolve o access token e escreve o refresh token em cookie HttpOnly.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request);
        if (result.refreshToken() != null) {
            ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refresh_token", result.refreshToken())
                    .httpOnly(true)
                    .secure(false) // defina true em produção com HTTPS
                    .path("/")
                    .sameSite("Lax");
            if (result.refreshMaxAgeSeconds() >= 0) {
                builder.maxAge(result.refreshMaxAgeSeconds());
            }
            ResponseCookie cookie = builder.build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } else {
            // Garantir que admins/super-admins não ficam com refresh cookie antigo
            ResponseCookie expired = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(0).build();
            response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
        }
        return ResponseEntity.ok(result.response());
    }

    /**
     * Passo 1 da recuperação: gera e envia o código.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        authService.requestPasswordReset(req.email());
        return ResponseEntity.noContent().build();
    }

    /**
     * Passo 3 da recuperação: valida código + nova password e conclui o processo.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        authService.resetPassword(req.email(), req.code(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Passo 2 da recuperação: confirma que o código ainda é válido antes de mostrar o formulário final.
     */
    @PostMapping("/reset-password/verify")
    public ResponseEntity<Void> verifyResetCode(@RequestBody @Valid VerifyResetCodeRequest req) {
        authService.validateResetCode(req.email(), req.code());
        return ResponseEntity.noContent().build();
    }

    /**
     * Reemite um novo access token com base no refresh token guardado em cookie.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String refresh = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refresh_token".equals(c.getName())) { refresh = c.getValue(); break; }
            }
        }
        LoginResponse resp = authService.refresh(refresh);
        return ResponseEntity.ok(resp);
    }

    /**
     * Termina sessão invalidando o refresh token e apagando o cookie no browser.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refresh_token".equals(c.getName())) { refresh = c.getValue(); break; }
            }
        }
        authService.revokeRefresh(refresh);
        ResponseCookie expired = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
        return ResponseEntity.noContent().build();
    }
}
