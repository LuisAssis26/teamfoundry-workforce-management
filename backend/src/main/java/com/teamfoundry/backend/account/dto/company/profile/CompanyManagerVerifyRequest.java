package com.teamfoundry.backend.account.dto.company.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Pedido para confirmar código e atualizar email do responsável.
 */
@Data
public class CompanyManagerVerifyRequest {

    @NotBlank
    @Email
    private String newEmail;

    @NotBlank
    @Size(min = 6, max = 6)
    private String code;

    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @NotBlank
    @Size(min = 6, max = 30)
    private String phone;

    @NotBlank
    @Size(min = 2, max = 100)
    private String position;
}
