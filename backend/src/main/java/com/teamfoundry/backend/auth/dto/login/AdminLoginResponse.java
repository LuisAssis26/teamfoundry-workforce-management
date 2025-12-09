package com.teamfoundry.backend.auth.dto.login;

import com.teamfoundry.backend.account.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO retornado com o papel do administrador autenticado.
 */
@Data
@AllArgsConstructor
public class AdminLoginResponse {

    private UserType role;
    private String accessToken;
    private long expiresInSeconds;
    private String refreshToken;
}
