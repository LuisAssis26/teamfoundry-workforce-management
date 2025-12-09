package com.teamfoundry.backend.superadmin.dto.credential.admin;

import com.teamfoundry.backend.account.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload enviado pelo super admin ao criar um novo administrador.
 */
public record AdminCredentialRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(min = 3, max = 60)
        String username,

        @NotBlank(message = "password é obrigatória")
        @Size(min = 8, max = 120)
        String password,

        @NotNull(message = "role é obrigatório")
        UserType role,

        @NotBlank(message = "superAdminPassword é obrigatório")
        String superAdminPassword
) {}

