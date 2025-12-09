package com.teamfoundry.backend.account.dto.employee.documents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeCertificationRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @NotBlank
    @Size(min = 2, max = 150)
    private String institution;

    @Size(max = 150)
    private String location;

    @NotNull
    @PastOrPresent
    private LocalDate completionDate;

    @Size(max = 500)
    private String description;

    /**
     * Conteúdo base64 do certificado. Pode incluir prefixo data-url.
     * Opcional em atualizações quando já existe um ficheiro armazenado.
     */
    private String certificateFile;

    /**
     * Nome original do ficheiro (para deduzir a extensão).
     */
    private String certificateFileName;
}
