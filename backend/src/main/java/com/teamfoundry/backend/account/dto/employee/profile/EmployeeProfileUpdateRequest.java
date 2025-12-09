package com.teamfoundry.backend.account.dto.employee.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Payload utilizado para atualizar os dados pessoais do candidato.
 */
@Getter
@Setter
public class EmployeeProfileUpdateRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^(?i)(male|female|other)$", message = "Género inválido.")
    private String gender;

    @NotNull
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate birthDate;

    @NotBlank
    @Size(min = 2, max = 100)
    private String nationality;

    @NotNull
    private Integer nif;

    @NotBlank
    @Pattern(regexp = "^[+]?\\d[\\d\\s().-]{7,}$", message = "Telefone inválido.")
    private String phone;

}


