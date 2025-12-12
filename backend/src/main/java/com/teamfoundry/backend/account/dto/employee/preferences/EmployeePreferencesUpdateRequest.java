package com.teamfoundry.backend.account.dto.employee.preferences;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmployeePreferencesUpdateRequest {

    @NotBlank(message = "A função é obrigatória.")
    @Size(max = 150, message = "A função não pode exceder 150 caracteres.")
    private String role;

    @NotEmpty(message = "Selecione pelo menos uma área geográfica.")
    private List<@NotBlank(message = "O nome da área não pode estar vazio.") String> areas;

    @NotEmpty(message = "Selecione pelo menos uma competência.")
    private List<@NotBlank(message = "O nome da competência não pode estar vazio.") String> skills;
}
