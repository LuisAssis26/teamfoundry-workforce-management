package com.teamfoundry.backend.superadmin.dto.credential.admin;

import com.teamfoundry.backend.account.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload usado para atualizar administradores.
 * O campo password é opcional; é aplicado apenas se vier preenchido.
 */
public record AdminCredentialUpdateRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(min = 3, max = 60)
        String username,

        @NotNull(message = "role é obrigatório")
        UserType role,

        @Size(min = 8, max = 120, message = "password deve possuir entre 8 e 120 caracteres")
        String password,

        @NotBlank(message = "superAdminPassword é obrigatório")
        String superAdminPassword
) {}

