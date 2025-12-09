package com.teamfoundry.backend.auth.dto.login;

public record LoginResponse(
        String userType,
        String message,
        String accessToken,
        long expiresInSeconds
) {
}
