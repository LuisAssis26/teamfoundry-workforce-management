package com.teamfoundry.backend.account.dto.employee.documents;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurriculumUploadRequest {
    /**
     * Conteúdo base64 do CV (pode incluir prefixo data-url).
     */
    @NotBlank
    private String file;

    /**
     * Nome original do ficheiro (para deduzir extensão).
     */
    @NotBlank
    private String fileName;
}
