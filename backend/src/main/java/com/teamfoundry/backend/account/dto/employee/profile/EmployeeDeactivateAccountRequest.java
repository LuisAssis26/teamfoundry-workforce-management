package com.teamfoundry.backend.account.dto.employee.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeDeactivateAccountRequest {
    @NotBlank(message = "Indique a password para confirmar.")
    private String password;
}
