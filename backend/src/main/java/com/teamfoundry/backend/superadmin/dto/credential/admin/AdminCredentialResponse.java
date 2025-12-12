package com.teamfoundry.backend.superadmin.dto.credential.admin;

import com.teamfoundry.backend.account.enums.UserType;

/**
 * Projeção simples usada para expor administradores
 * ao painel do super admin sem expor o hash da password.
 */
public record AdminCredentialResponse(
        int id,
        String username,
        UserType role
) {}
