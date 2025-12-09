package com.teamfoundry.backend.account.dto.employee.documents;

import com.teamfoundry.backend.account.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentificationDocumentUploadRequest {

    @NotBlank
    private String file;

    @NotBlank
    private String fileName;

    @NotNull
    private DocumentType type;
}
