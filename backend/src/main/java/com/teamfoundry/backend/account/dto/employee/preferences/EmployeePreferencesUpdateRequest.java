package com.teamfoundry.backend.account.dto.employee.preferences;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmployeePreferencesUpdateRequest {

    @Size(max = 150, message = "A funcao nao pode exceder 150 caracteres.")
    private String role;

    private List<String> roles;

    private List<String> areas;

    private List<String> skills;
}
