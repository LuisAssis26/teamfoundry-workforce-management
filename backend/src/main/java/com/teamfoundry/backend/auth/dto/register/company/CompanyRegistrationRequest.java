package com.teamfoundry.backend.auth.dto.register.company;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CompanyRegistrationRequest(
        @Email(message = "Insira um e-mail de acesso válido.")
        @NotBlank(message = "O e-mail de acesso é obrigatório.")
        String credentialEmail,

        @NotBlank(message = "A password é obrigatória.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
                message = "A password deve ter 8 a 64 caracteres e incluir maiúsculas, minúsculas, número e símbolo."
        )
        String password,

        @NotBlank(message = "Indique o nome do responsável.")
        @Size(max = 120, message = "O nome do responsável é demasiado longo.")
        String responsibleName,

        @NotBlank(message = "Indique o cargo do responsável.")
        @Size(max = 120, message = "O cargo do responsável é demasiado longo.")
        String responsiblePosition,

        @Email(message = "Insira um e-mail corporativo válido.")
        @NotBlank(message = "O e-mail corporativo é obrigatório.")
        String responsibleEmail,

        @NotBlank(message = "O telefone do responsável é obrigatório.")
        @Pattern(regexp = "^[0-9+()\\s-]{6,20}$", message = "Telefone inválido.")
        String responsiblePhone,

        @NotBlank(message = "Indique o nome da empresa.")
        String companyName,

        @NotNull(message = "Indique o NIF.")
        @Digits(integer = 9, fraction = 0, message = "O NIF deve conter apenas dígitos (até 9).")
        Integer nif,

        @NotEmpty(message = "Selecione pelo menos uma área de atividade.")
        List<String> activitySectors,

        @NotBlank(message = "Selecione o país.")
        String country,

        @NotBlank(message = "Indique a morada.")
        String address,

        String website,
        String description,

        @NotNull(message = "É necessário aceitar os termos e condições.")
        Boolean termsAccepted
) {}
