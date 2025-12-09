package com.teamfoundry.backend.account_options.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeactivateAccountRequest {
    @NotBlank(message = "Indique a password para confirmar.")
    private String password;
}
