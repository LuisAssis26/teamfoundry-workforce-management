package com.teamfoundry.backend.superadmin.dto.credential.company;

/**
 * Representa a resposta mínima necessária para exibir
 * uma credencial empresarial pendente no painel do super admin.
 * Optei por um "record" porque ele já provê imutabilidade e
 * gera automaticamente equals/hashCode/toString para uso em DTOs.
 */
public record CompanyCredentialResponse(
        int id,
        String companyName,
        String credentialEmail,
        String website,
        String address,
        int nif,
        String country,
        String responsibleName,
        String responsibleEmail,
        String responsiblePhone,
        String responsiblePosition
) {}
