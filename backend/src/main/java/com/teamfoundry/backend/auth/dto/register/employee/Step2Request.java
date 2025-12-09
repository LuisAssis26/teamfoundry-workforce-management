package com.teamfoundry.backend.auth.dto.register.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Step2Request {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "A nacionalidade é inválida.")
    private String nationality;

    @NotNull
    private LocalDate birthDate;

    @NotBlank
    @Pattern(regexp = "^[+]?\\d[\\d\\s().-]{7,}$", message = "Phone must be a valid number.")
    private String phone;

    @NotNull
    private Integer nif;

    /**
     * Conteúdo base64 do ficheiro de CV (opcional). Pode incluir prefixo data-url.
     */
    private String cvFile;

    /**
     * Nome original do ficheiro (opcional), utilizado para derivar a extensão ao guardar.
     */
    private String cvFileName;
}
