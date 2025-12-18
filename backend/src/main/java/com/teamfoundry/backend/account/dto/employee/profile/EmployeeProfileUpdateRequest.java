package com.teamfoundry.backend.account.dto.employee.profile;

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

    @Size(min = 2, max = 100)
    private String firstName;

    @Size(min = 2, max = 100)
    private String lastName;

    @Pattern(regexp = "^(?i)(male|female|other)$", message = "Gênero inválido.")
    private String gender;

    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate birthDate;

    @Size(min = 2, max = 100)
    private String nationality;

    private Integer nif;

    @Pattern(regexp = "^[+]?\\d[\\d\\s().-]{7,}$", message = "Telefone inválido.")
    private String phone;

}
