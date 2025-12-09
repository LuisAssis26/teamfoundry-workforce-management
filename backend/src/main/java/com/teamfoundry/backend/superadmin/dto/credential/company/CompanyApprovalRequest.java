package com.teamfoundry.backend.superadmin.dto.credential.company;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload usado para aprovar credenciais empresariais.
 */
public record CompanyApprovalRequest(
        @NotBlank(message = "superAdminPassword é obrigatório")
        String superAdminPassword
) {}
