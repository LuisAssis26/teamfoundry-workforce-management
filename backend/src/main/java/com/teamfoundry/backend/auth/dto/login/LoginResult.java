package com.teamfoundry.backend.auth.dto.login;

/**
 * Internal result from AuthService.login used by controller to
 * optionally set the refresh-token cookie without exposing it in the body.
 */
public record LoginResult(
        LoginResponse response,
        String refreshToken,
        int refreshMaxAgeSeconds
) {}

