package com.teamfoundry.backend.account.dto.company.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Pedido para atualizar os dados do responsável pela conta da empresa.
 */
@Data
public class CompanyManagerUpdateRequest {

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
