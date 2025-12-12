package com.teamfoundry.backend.superadmin.dto.credential.admin;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload usado para desativar administradores.
 */
public record AdminCredentialDisableRequest(
        @NotBlank(message = "superAdminPassword é obrigatório")
        String superAdminPassword
) {}
